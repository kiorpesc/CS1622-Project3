package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRCopy extends IRQuadruple{

  public IRCopy(SymbolInfo arg1, SymbolInfo result)
  {
    super(null, arg1, null, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.getName());
      output.append(" := ");
      output.append(_arg1.getName());
      return output.toString();
  }

  public void accept(IRVisitor g)
  {
    g.visit(this);
  }
}
