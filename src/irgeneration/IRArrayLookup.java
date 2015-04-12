package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRArrayLookup extends IRQuadruple{

  public IRArrayLookup(SymbolInfo arg1, SymbolInfo arg2, SymbolInfo result)
  {
    super(null, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.getName());
      output.append(" := ");
      output.append(_arg1.getName());
      output.append("[");
      output.append(_arg2.getName());
      output.append("]");
      return output.toString();
  }

  public void accept(IRVisitor g)
  {
    g.visit(this);
  }
}
