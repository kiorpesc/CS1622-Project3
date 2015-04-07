package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRNewArray extends IRQuadruple{

  private String _type;

  public IRNewArray(SymbolInfo arg2, SymbolInfo result)
  {
    super("new", null, arg2, result);
    _type = "int";
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.getName());
      output.append(" := ");
      output.append(_op);
      output.append(" ");
      output.append(_type);
      output.append(", ");
      output.append(_arg2.getName());
      return output.toString();
  }

  public void accept(CodeGenerator g)
  {
    g.visit(this);
  }
}
