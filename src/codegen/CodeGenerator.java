package codegen;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import irgeneration.*;
import objectimpl.*;
import symboltable.*;
import regalloc.*;
import controlflow.*;

import java.io.*;
import java.util.Scanner;


public class CodeGenerator implements IRVisitor {

  // needs to be <SymbolInfo, String>
  private HashMap<String, String> _registerMap;
  private Map<MethodSymbol, ControlFlowGraph> _cfgMap;
  private int _nextIntermediateValue;
  private int _nextTempReg;
  private int _currentParam;
  private List<String> _mips;
  private List<IRQuadruple> _irList;
  private int _minRegister;
  private ControlFlowGraph _currentMethodCfg;
  private LivenessAnalysis _currentMethodLiveness;
  private InterferenceGraph _currentMethodInterferenceGraph;
  private RegisterAllocator _regAllocator;
  private int _numRegs;
  private ObjectLayoutManager _objLayoutMgr;
  private MethodSymbol _currentMethod = null;

  public CodeGenerator(List<IRQuadruple> irList, Map<MethodSymbol, ControlFlowGraph> cfgs, ObjectLayoutManager objLayoutMgr)
  {
    _objLayoutMgr = objLayoutMgr;
    //_jumpMap = new Stack<HashMap<String, String>>();
    _registerMap = new HashMap<String, String>();
    // $8 == $t0
    _nextTempReg = 8;
    _currentParam = 0;
    _mips = new ArrayList<String>();
    _irList = irList;
    _cfgMap = cfgs;
    _minRegister = 8;  // right now hard coding lowest register
    _numRegs = 22;
  }

  // walk IR list, generate basic MIPS for each statement

  public void generateCode()
  {
    for(int i = 0; i < _irList.size(); i++)
    {
      _irList.get(i).accept(this);
    }
    // link provided library
  }

  private String getTempRegister(String varName)
  {
    // the $t* and $s* registers are in [8, 25]
    // TODO: utilize other registers as per Milestone 4
    if (_nextTempReg > 25)
    {
      System.err.println("Too many temporaries, exiting.");
      System.exit(1);
    }

    String regName = "$"+_nextTempReg++;
    _registerMap.put(varName, regName);
    return regName;
  }

  private String getAllocatedRegister(SymbolInfo var)
  {
    Integer regColor = _regAllocator.getColor(var);
    if(regColor == null)
      return null;

    regColor += _minRegister;
    String regString = "$" + regColor;
    return regString;
  }

  private String getParamRegister()
  {
    if (_currentParam == 4)
    {
      System.err.println("Too many parameters, exiting.");
      System.exit(1);
    }
    return "$a" + _currentParam++;
  }

  private String generateMove(String dest, String source)
  {
    if (dest.equals(source))
      return "";

    return generateInstruction("add", dest, source, "$zero");
  }

  private String generateInstruction(String op, String dest, String arg1, String arg2)
  {
    StringBuilder result = new StringBuilder();
    result.append(op);
    result.append(' ');
    result.append(dest);
    result.append(", ");
    result.append(arg1);
    result.append(", ");
    result.append(arg2);
    result.append('\n');
    return result.toString();
  }

   // save all current registers to the stack
  private void saveAllRegisters()
  {
    // 32 * 4 = 128

    StringBuilder inst = new StringBuilder();


    inst.append("addi $sp, $sp, -128\n");
    for(int i = 8; i < 32; i++) // $8 is $t0
    {
      inst.append("sw $");
      inst.append(i);
      inst.append(" ");
      inst.append((4*(i-8)));
      inst.append("($sp)\n");
    }
    _mips.add(inst.toString());
  }

  // load from the stack into registers
  private void loadAllRegisters()
  {
    StringBuilder inst = new StringBuilder();
    for(int i = 31; i >= 8; i--)
    {
      inst.append("lw $");
      inst.append(i);
      inst.append(" ");
      inst.append((4*(i-8)));
      inst.append("($sp)\n");
    }
    inst.append("addi $sp, $sp, 128");
    _mips.add(inst.toString());
  }

  private void clearRegisterMap()
  {
    _registerMap = new HashMap<String,String>();
    _nextTempReg = 8;
  }

    public String getRegisterByName(String name)
  {
    if(_registerMap.containsKey(name))
    {
      return _registerMap.get(name);
    } else {
      return getTempRegister(name);
    }
  }

  private String getValueFromConstant(ConstantSymbol sym)
  {
    String value = sym.getValue();
    // check for boolean
    switch (value.toLowerCase())
    {
      case "true":
        value = "1";
        break;
      case "false":
        value = "0";
        break;
    }
    return value;
  }

  private String getRegisterForValue(StringBuilder inst, SymbolInfo sym)
  {
    if (sym instanceof ConstantSymbol)
    {
      inst.append("li $v1, ");
      inst.append(getValueFromConstant((ConstantSymbol)sym));
      inst.append('\n');
      return "$v1";
    }
    else if (_objLayoutMgr.isInstanceVariable(sym))
    {
      String regName = getAllocatedRegister(sym);
      inst.append("lw ");
      inst.append(regName);
      inst.append(", ");
      inst.append(_objLayoutMgr.getByteOffset(sym));
      inst.append("(");
      inst.append(getAllocatedRegister(_currentMethod.getVariable("this")));
      inst.append(")\n");
      return regName;
    }
    return getAllocatedRegister(sym);
  }

  private void instanceVariableAssignment(StringBuilder inst, SymbolInfo result)
  {
    // TODO: get the register 'this' was allocated to
    // TODO: handle the case where the result might be spilled?
    if (_objLayoutMgr.isInstanceVariable(result))
    {
      inst.append("\nsw ");
      inst.append(getAllocatedRegister(result));
      inst.append(", ");
      inst.append(_objLayoutMgr.getByteOffset(result));
      inst.append("(");
      inst.append(getAllocatedRegister(_currentMethod.getVariable("this")));
      inst.append(")\n");
    }
  }

  public void visit(IRArrayAssign n)
  {
    // TODO: handle constants
    StringBuilder inst = new StringBuilder();

    String arrayReg = getRegisterForValue(inst, n.getArg1());

    // index might be literal, so get a (possibly temp) register
    String indexReg = getRegisterForValue(inst, n.getArg2());

    // hack: we actually need two additional registers for an array assignment;
    // one to hold the calculated address, and one to hold the variable to assign
    String tempReg = "$t9";

    // move index into temporary register
    inst.append(generateMove(tempReg, indexReg));
    // add 1 to the index (move beyond length of array);
    inst.append(generateInstruction("add", tempReg, tempReg, "1"));
    // left shift the index by 2
    inst.append(generateInstruction("sll", tempReg, tempReg, "2"));
    // add index to array address
    inst.append(generateInstruction("add", tempReg, arrayReg, tempReg));

    // get our result
    String resultReg = getRegisterForValue(inst, n.getResult());

    // store the result register into the array
    inst.append("sw ");
    inst.append(resultReg);
    inst.append(", (");
    inst.append(tempReg);
    inst.append(")\n");

    _mips.add(inst.toString());
  }

  public void visit(IRArrayLength n)
  {
    StringBuilder inst = new StringBuilder();

    String resultReg = getAllocatedRegister(n.getResult());
    String arrayReg = getRegisterForValue(inst, n.getArg1());

    // load a word
    inst.append("lw ");
    // into the result register
    inst.append(resultReg);
    // from 0 bytes into the array
    inst.append(", (");
    inst.append(arrayReg);
    inst.append(")\n");

    instanceVariableAssignment(inst, n.getResult());
    _mips.add(inst.toString());
  }

  public void visit(IRArrayLookup n)
  {
    //TODO: handle constants
    StringBuilder inst = new StringBuilder();
    String resultReg = getAllocatedRegister(n.getResult());
    String arrayReg = getRegisterForValue(inst, n.getArg1());

    String indexReg = getRegisterForValue(inst, n.getArg2());

    String tempReg = "$v1";

    // move index into temporary register
    inst.append(generateMove(tempReg, indexReg));
    // add 1 to the index (move beyond length of array);
    inst.append(generateInstruction("add", tempReg, tempReg, "1"));
    // left shift the index by 2
    inst.append(generateInstruction("sll", tempReg, tempReg, "2"));
    // add index to array address
    inst.append(generateInstruction("add", tempReg, arrayReg, tempReg));

    // load the value from the array into the result
    inst.append("lw ");
    inst.append(resultReg);
    inst.append(", (");
    inst.append(tempReg);
    inst.append(")\n");

    _mips.add(inst.toString());
  }

  public void visit(IRAssignment n)
  {
    StringBuilder inst = new StringBuilder();

    String arg1RegName = getRegisterForValue(inst, n.getArg1());
    String arg2RegName = getRegisterForValue(inst, n.getArg2());
    String resultVarName = n.getResult().getName();

    if(n.getOp() == "*")
    {
      inst.append("mult ");
      inst.append(arg1RegName);
      inst.append(", ");
      inst.append(arg2RegName);
      inst.append("\nmflo ");
      inst.append(getAllocatedRegister(n.getResult()));
    } else {

      switch(n.getOp()) {
        case "+" : inst.append("add ");
                 break;
        case "-" : inst.append("sub ");
                 break;
        case "&&" : inst.append("and ");
                 break;
        case "<"  : inst.append("slt ");
      }
      inst.append(getAllocatedRegister(n.getResult()));
      inst.append(", ");
      inst.append(arg1RegName);
      inst.append(", ");
      inst.append(arg2RegName);
    }
    instanceVariableAssignment(inst, n.getResult());
    _mips.add(inst.toString());
  }

  public void visit(IRCall n)
  {
    StringBuilder inst = new StringBuilder();

    // TODO: calling convention
    // load args into $a0-3

    // jal to label
    inst.append("jal ");
    MethodSymbol meth = (MethodSymbol)(n.getArg1());



    inst.append(meth.getLabel());

    _mips.add(inst.toString());

    _currentParam = 0;  // now that we're past the jump instruction, parameter count is reset

    // now need to get result of call
    if(n.getResult() != null)
    {
      String v0Reg = getAllocatedRegister(n.getResult());
      inst = new StringBuilder("add ");
      inst.append(v0Reg);
      inst.append(", $v0, $zero");
      instanceVariableAssignment(inst, n.getResult());
      _mips.add(inst.toString());
    }
  }

  public void visit(IRCondJump n)
  {
      // beq $reg 0 LABEL
      StringBuilder inst = new StringBuilder();
      String condReg = getRegisterForValue(inst, n.getArg1());
      inst.append("beq ");
      inst.append(condReg);
      inst.append(", $zero, ");
      inst.append(n.getLabel());
      _mips.add(inst.toString());
  }

  public void visit(IRCopy n)
  {
    StringBuilder inst = new StringBuilder();
    if(n.getArg1() instanceof ConstantSymbol)
    {
      inst.append("li ");
      inst.append(getAllocatedRegister(n.getResult()));
      inst.append(", ");
      inst.append(getValueFromConstant((ConstantSymbol)n.getArg1()));
    } else {
      String arg1RegName = getRegisterForValue(inst, n.getArg1());
      inst.append("add ");
      inst.append(getAllocatedRegister(n.getResult()));
      inst.append(", ");
      inst.append(arg1RegName);
      inst.append(", $zero");
    }

    instanceVariableAssignment(inst, n.getResult());
    _mips.add(inst.toString());
  }

  private void allocateForCurrentMethod(MethodSymbol method)
  {
    _currentMethodCfg = _cfgMap.get(method);
    //System.out.println(_currentMethodCfg);
    _currentMethodLiveness = new LivenessAnalysis(_currentMethodCfg, method, _objLayoutMgr);
    //System.out.println(_currentMethodLiveness);
    _currentMethodInterferenceGraph = new InterferenceGraph(_currentMethodLiveness, _currentMethodCfg, _objLayoutMgr);
    //System.out.println(_currentMethodInterferenceGraph);
    _regAllocator = new RegisterAllocator(_currentMethodInterferenceGraph, _numRegs);
    //System.out.println(_regAllocator);
  }

  public void visit(IRLabel n)
  {
    _mips.add(n.toString());

    if(n.isMethod())
    {
      MethodSymbol method = n.getMethod();
      // could do liveness analysis and register allocation here

      allocateForCurrentMethod(method);
      StringBuilder inst;
      if(method.getName() != "main")
      {
        saveAllRegisters();
        clearRegisterMap();

        // this FORCES the 'this' variable to be allocated a register - not ideal
        VariableSymbol thisVar = method.getVariable("this");
        String thisReg = getAllocatedRegister(thisVar);
        if(thisReg != null) {
          inst = new StringBuilder("add ");
          inst.append(thisReg);
          inst.append(", $a0, $zero");
          _mips.add(inst.toString());
        }
      }

      // move arguments into non-argument registers for safety
      ArrayList<String> formals = method.getFormalNames();

      for(int i = 0; i < formals.size(); i++)
      {
        inst = new StringBuilder("add ");
        inst.append(getAllocatedRegister(method.getVariable(formals.get(i))));
        inst.append(", $a");
        inst.append((i+1));
        inst.append(", $zero");
        _mips.add(inst.toString());
      }
      _currentMethod = method;
    }
  }

  public void visit(IRNewArray n)
  {
    StringBuilder inst = new StringBuilder();
    // get register for the result of the new
    String resultReg = getAllocatedRegister(n.getResult());
    // get register that holds the size value
    String sizeReg = getRegisterForValue(inst, n.getArg2());

    // left shift size by 2 (multiplies by 4) and store in the argument register
    inst.append(generateInstruction("sll", getParamRegister(), sizeReg, "2"));

    // HACK: _new_array routine clobbers $t0 and $t1 and we don't have
    // saving working properly, so just save/load them for now.
    inst.append("addi $sp, $sp, -8\nsw $t0, 4($sp)\nsw $t1, 0($sp)\n");
    // jump to the new array routine
    inst.append("jal _new_array\n");
    inst.append("lw $t0, 4($sp)\nlw $t1, 0($sp)\naddi $sp, $sp, 8\n");
    // move the address to the result register
    inst.append(generateMove(resultReg, "$v0"));

    // reset number of parameters being used.
    _currentParam = 0;
    instanceVariableAssignment(inst, n.getResult());
    _mips.add(inst.toString());
  }

  public void visit(IRNewObject n)
  {
    // reserve space for when new object is implemented
    String resultReg = getAllocatedRegister(n.getResult());
    StringBuilder inst = new StringBuilder();

    String byteSize = "" + _objLayoutMgr.getSizeInBytes(n.getArg1());
    inst.append(generateInstruction("addi", getParamRegister(), "$0", byteSize));

    // HACK: _new_object routine clobbers $t0 and $t1 and we don't have
    // saving working properly, so just save/load them for now.
    inst.append("addi $sp, $sp, -8\nsw $t0, 4($sp)\nsw $t1, 0($sp)\n");
    inst.append("jal _new_object\n");
    inst.append("lw $t0, 4($sp)\nlw $t1, 0($sp)\naddi $sp, $sp, 8\n");
    // move the address to the ersult register
    inst.append(generateMove(resultReg, "$v0"));

    // reset number of parameters being used.
    _currentParam = 0;

    instanceVariableAssignment(inst, n.getResult());
    _mips.add(inst.toString());

  }

  public void visit(IRParam n)
  {
    StringBuilder inst = new StringBuilder();
    if (n.getArg1() instanceof ConstantSymbol)
    {
      inst.append("li ");
      inst.append(getParamRegister());
      inst.append(", ");
      inst.append(getValueFromConstant((ConstantSymbol)n.getArg1()));
    }
    else
    {
      String arg1RegName = getRegisterForValue(inst, n.getArg1());
      inst.append("add ");
      inst.append(getParamRegister());
      inst.append(", ");
      inst.append(arg1RegName);
      inst.append(", $zero");
    }
    _mips.add(inst.toString());
  }

  public void visit(IRReturn n)
  {
    String retName = n.getArg1().getName();
    // store result in $v0
    StringBuilder retInst = new StringBuilder();
    String retReg = getRegisterForValue(retInst, n.getArg1());
    retInst.append("add $v0, ");
    retInst.append(retReg);
    retInst.append(", $zero");
    _mips.add(retInst.toString());

    loadAllRegisters();
    _mips.add("jr $ra");

    // TODO: calling convention

  }

  public void visit(IRUnaryAssignment n)
  {
    String resultReg = getAllocatedRegister(n.getResult());
    StringBuilder inst = new StringBuilder();
    String arg1RegName = getRegisterForValue(inst, n.getArg1());
    inst.append("nor ");
    inst.append(resultReg);
    inst.append(", ");
    inst.append(arg1RegName);
    inst.append(", $0\n");
    inst.append("andi ");
    inst.append(resultReg);
    inst.append(", ");
    inst.append(resultReg);
    inst.append(", 1");
    instanceVariableAssignment(inst, n.getResult());
    _mips.add(inst.toString());
  }

  public void visit(IRUncondJump n)
  {
    // j LABEL
    StringBuilder inst = new StringBuilder("j ");
    inst.append(n.getLabel());
    _mips.add(inst.toString());
  }

  public void printCode()
  {
    for(String inst : _mips)
    {
      System.out.println(inst);
    }
  }

  public void printRegisterMap()
  {
    System.out.println("----- REGISTER MAP -----");
    for(String key : _registerMap.keySet())
    {
      System.out.println(key + " : " + _registerMap.get(key));
    }
  }

  public void outputMIPSFile(String outputFileName) throws FileNotFoundException
  {
    String libraryFile = "lib/runtime.asm";
    PrintWriter outputFile = new PrintWriter(new File(outputFileName));

    for(String inst : _mips)
    {
      outputFile.println(inst);
    }

    outputFile.println(); // a little space for readability

    Scanner library = new Scanner(new FileReader(libraryFile));
    while(library.hasNextLine())
      outputFile.println(library.nextLine());

    library.close();
    outputFile.close();

  }

}
