package codegen;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

import irgeneration.*;
import symboltable.*;

import java.io.*;
import java.util.Scanner;


public class CodeGenerator {

  //private Stack<HashMap<String, String>> _jumpMap;
  private HashMap<String, String> _registerMap;
  private int _nextTempReg;
  private int _currentParam;
  private ArrayList<String> _mips;
  private ArrayList<IRQuadruple> _irList;

  public CodeGenerator(ArrayList<IRQuadruple> irList)
  {
    //_jumpMap = new Stack<HashMap<String, String>>();
    _registerMap = new HashMap<String, String>();
    // $8 == $t0
    _nextTempReg = 8;
    _currentParam = 0;
    _mips = new ArrayList<String>();
    _irList = irList;
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

  private String getParamRegister()
  {
    if (_currentParam == 4)
    {
      System.err.println("Too many parameters, exiting.");
      System.exit(1);
    }
    return "$a" + _currentParam++;
  }

  public void visit(IRArrayAssign n)
  {

  }

  public void visit(IRArrayLength n)
  {

  }

  public void visit(IRArrayLookup n)
  {

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

  public void visit(IRAssignment n)
  {
    String arg1VarName = n.getArg1().getName();
    String arg2VarName = n.getArg2().getName();
    String resultVarName = n.getResult().getName();

    StringBuilder inst = new StringBuilder();

    if(n.getOp() == "*")
    {
      inst.append("mult ");
      inst.append(getRegisterByName(arg1VarName));
      inst.append(", ");
      inst.append(getRegisterByName(arg2VarName));
      inst.append("\nmflo ");
      inst.append(getRegisterByName(resultVarName));
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
      inst.append(getRegisterByName(resultVarName));
      inst.append(", ");
      inst.append(getRegisterByName(arg1VarName));
      inst.append(", ");
      inst.append(getRegisterByName(arg2VarName));
    }
    _mips.add(inst.toString());
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
      inst.append((4*i));
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
      inst.append((4*i));
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
      String v0Reg = getRegisterByName(n.getResult().getName());
      inst = new StringBuilder("add ");
      inst.append(v0Reg);
      inst.append(", $v0, $zero");
      _mips.add(inst.toString());
    }



  }

  public void visit(IRCondJump n)
  {
      String condVarName = n.getArg1().getName();
      // beq $reg 0 LABEL
      StringBuilder inst = new StringBuilder("beq ");
      inst.append(getRegisterByName(condVarName));
      inst.append(", $zero, ");
      inst.append(n.getLabel());
      _mips.add(inst.toString());
  }

  public void visit(IRCopy n)
  {
    StringBuilder inst = new StringBuilder();
    if(n.getArg1().getSymbolType() == "constant")
    {
      inst.append("li ");
      inst.append(getRegisterByName(n.getResult().getName()));
      inst.append(", ");
      inst.append(n.getArg1().getName());
    } else {
      inst.append("add ");
      inst.append(getRegisterByName(n.getResult().getName()));
      inst.append(", ");
      inst.append(getRegisterByName(n.getArg1().getName()));
      inst.append(", $zero");
    }

    _mips.add(inst.toString());
  }

  public void visit(IRLabel n)
  {
    _mips.add(n.toString());
    if(n.isMethod())
    {
      saveAllRegisters();
      clearRegisterMap();

      // move arguments into non-argument registers for safety
      StringBuilder inst = new StringBuilder("add ");
      inst.append(getRegisterByName("this"));
      inst.append(", $a0, $zero");
      _mips.add(inst.toString());

      ArrayList<String> formals = n.getMethod().getFormalNames();

      for(int i = 0; i < formals.size(); i++)
      {
        inst = new StringBuilder("add ");
        inst.append(getRegisterByName(formals.get(i)));
        inst.append(", $a");
        inst.append((i+1));
        inst.append(", $zero");
        _mips.add(inst.toString());
      }
    }
  }

  public void visit(IRNewArray n)
  {

  }

  public void visit(IRNewObject n)
  {
    // reserve space for when new object is implemented
    String resultReg = getRegisterByName(n.getResult().getName());
  }

  public void visit(IRParam n)
  {
    String arg1VarName = n.getArg1().getName();
    StringBuilder inst = new StringBuilder();
      inst.append("add ");
      inst.append(getParamRegister());
      inst.append(", ");
      inst.append(getRegisterByName(arg1VarName));
      inst.append(", $zero");
      _mips.add(inst.toString());
  }

  public void visit(IRReturn n)
  {
    String retName = n.getArg1().getName();
    // store result in $v0
    StringBuilder retInst = new StringBuilder("add $v0, ");
    retInst.append(getRegisterByName(retName));
    retInst.append(", $zero");
    _mips.add(retInst.toString());

    loadAllRegisters();
    _mips.add("jr $ra");

    // TODO: calling convention

  }

  public void visit(IRUnaryAssignment n)
  {

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
