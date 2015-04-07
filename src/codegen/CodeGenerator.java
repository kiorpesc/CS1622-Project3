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

  public void visit(IRQuadruple n)
  {
    // this is disgusting.

    if(n instanceof IRCall)
    {
      visit((IRCall)n);
    } else if (n instanceof IRParam)
    {
      visit((IRParam)n);
    }
    else if (n instanceof IRLabel)
    {
      visit((IRLabel)n);
    }
    else
    {
      System.out.println("Didn't get correct subclass.");
    }
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

  }

  public void visit(IRAssignment n)
  {

  }

  public void visit(IRCopy n)
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

  public void printCode()
  {
    for(String inst : _mips)
    {
      System.out.println(inst);
    }
  }

  public void visit(IRLabel n)
  {
    _mips.add(n.toString());
  }
}
