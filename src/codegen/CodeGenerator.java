package codegen;

import java.util.HashMap;
import java.util.ArrayList;

import irgeneration.*;
import symboltable.*;

import java.io.*;
import java.util.Scanner;


public class CodeGenerator {

  private HashMap<String, String> _registerMap;
  private int _regCount;
  private int _currentParam;
  private ArrayList<String> _mips;
  private ArrayList<IRQuadruple> _irList;
  private int _tempCount;

  public CodeGenerator(ArrayList<IRQuadruple> irList)
  {
    _registerMap = new HashMap<String, String>();
    _regCount = 0;
    _currentParam = 0;
    _mips = new ArrayList<String>();
    _irList = irList;
    _tempCount = 0;
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
    String regName = "$t"+_regCount++;
    _registerMap.put(varName, regName);
    return regName;
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

  public void visit(IRAssignment n)
  {
      String arg1VarName = n.getArg1().getName();
      String arg2VarName = n.getArg2().getName();
      String resultVarName = n.getResult().getName();

      StringBuilder inst = new StringBuilder();
      switch(n.getOp()) {
        case "+" : inst.append("add ");
                   break;
        case "-" : inst.append("sub ");
                   break;
        case "*" : inst.append("mult ");
                   break;
        case "&&" : inst.append("and ");
                   break;
        case "<"  : inst.append("slt ");
      }
      inst.append(getTempRegister(resultVarName));
      inst.append(", ");
      inst.append(_registerMap.get(arg1VarName));
      inst.append(", ");
      inst.append(_registerMap.get(arg2VarName));
      _mips.add(inst.toString());
  }

  public void visit(IRCall n)
  {
    StringBuilder inst = new StringBuilder();
    // load args into $a0-3
    // save current regs to stack
    // save return address on the stack
    // jal to label
    inst.append("jal ");
    MethodSymbol meth = (MethodSymbol)(n.getArg1());
    inst.append(meth.getLabel());

    _mips.add(inst.toString());

    _currentParam = 0;  // now that we have jumped, parameter count is reset

  }

  public void visit(IRCondJump n)
  {

  }

  public void visit(IRCopy n)
  {
    StringBuilder inst = new StringBuilder();
    if(n.getArg1().getSymbolType() == "constant")
    {
      inst.append("li ");
      inst.append(getTempRegister(n.getResult().getName()));
      inst.append(", ");
      inst.append(n.getArg1().getName());
    } else {
      inst.append("add ");
      inst.append(getTempRegister(n.getResult().getName()));
      inst.append(", ");
      inst.append(n.getArg1().getName());
      inst.append(", $zero");
    }

    _mips.add(inst.toString());
  }

  public void visit(IRLabel n)
  {
    _mips.add(n.toString());
  }

  public void visit(IRNewArray n)
  {

  }

  public void visit(IRNewObject n)
  {

  }

  public void visit(IRParam n)
  {
    String arg1VarName = n.getArg1().getName();
    StringBuilder inst = new StringBuilder();
      inst.append("add ");
      inst.append("$a");
      inst.append(_currentParam++);
      inst.append(", ");
      inst.append(_registerMap.get(arg1VarName));
      inst.append(", $zero");
      _mips.add(inst.toString());
  }

  public void visit(IRReturn n)
  {

  }

  public void visit(IRUnaryAssignment n)
  {

  }

  public void visit(IRUncondJump n)
  {

  }

  public void printCode()
  {
    for(String inst : _mips)
    {
      System.out.println(inst);
    }
  }

  public void printRegMap()
  {
    for(String key : _registerMap.keySet())
    {
      System.out.println(key + " : " + _registerMap.get(key));
    }
  }

  public void outputMIPSFile(String outputFileName) throws FileNotFoundException
  {
    String libraryFile = "runtime.asm";
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
