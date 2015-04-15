package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRArrayAssign extends IRQuadruple{

  public IRArrayAssign(SymbolInfo arg1, SymbolInfo arg2, SymbolInfo result)
  {
    super(null, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_arg1.getName());
      output.append("[");
      output.append(_arg2.getName());
      output.append("]");
      output.append(" := ");
      output.append(_result.getName());
      return output.toString();
  }

  public boolean replaceArgs(SymbolInfo toReplace, SymbolInfo replaceWith)
  {
    boolean changed = super.replaceArgs(toReplace, replaceWith);

    if (_result == toReplace)
    {
      changed = true;
      _result = replaceWith;
    }

    return changed;
  }

  public boolean isDefOf(SymbolInfo sym)
  {
    return false;
  }

  public boolean isUsageOf(SymbolInfo sym)
  {
    return super.isUsageOf(sym) || _result == sym;
  }

  public void accept(IRVisitor g)
  {
    g.visit(this);
  }
}
