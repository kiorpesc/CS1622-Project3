package irgeneration;

import symboltable.*;
import syntaxtree.*;
import codegen.CodeGenerator;

// the generic storage format for IR
public abstract class IRQuadruple {

  protected String _op;
  protected SymbolInfo _arg1;
  protected SymbolInfo _arg2;
  protected SymbolInfo _result;

  public IRQuadruple(String op, SymbolInfo arg1, SymbolInfo arg2, SymbolInfo result)
  {
    _op = op;
    _arg1 = arg1;
    _arg2 = arg2;
    _result = result;
  }

  // utility function to modify the result after creation
  public void setResult(SymbolInfo result)
  {
    _result = result;
  }

  // probably unnecessary
  public abstract String toString();

  public String getOp()
  {
    return _op;
  }

  public SymbolInfo getArg1()
  {
    return _arg1;
  }

  public SymbolInfo getArg2()
  {
    return _arg2;
  }

  public SymbolInfo getResult()
  {
    return _result;
  }

  // replace the arguments with replaceWith, if any of them are
  // toReplace.
  public boolean replaceArgs(SymbolInfo toReplace, SymbolInfo replaceWith)
  {
    boolean result = false;

    if (_arg1 == toReplace)
    {
      _arg1 = replaceWith;
      result = true;
    }

    if (_arg2 == toReplace)
    {
      _arg2 = replaceWith;
      result = true;
    }

    return result;
  }

  public abstract void accept(IRVisitor g);

}
