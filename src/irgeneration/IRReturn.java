package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRReturn extends IRQuadruple{

  public IRReturn(SymbolInfo arg1)
  {
    super("return", arg1, null, null);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_op);
      output.append(" ");
      output.append(_arg1.getName());
      return output.toString();
  }

  public void accept(IRVisitor g)
  {
    g.visit(this);
  }
}
