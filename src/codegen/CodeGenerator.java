package codegen;

import java.util.HashMap;
import java.util.ArrayList;

import irgeneration.*;
import symboltable.*;

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
    StringBuilder inst = new StringBuilder();
    if(n.getArg1().getSymbolType() == "constant")
    {
      inst.append("li ");
      inst.append("$a");
      inst.append(_currentParam++);
      inst.append(", ");
      inst.append(n.getArg1().getName());
    }
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


}
