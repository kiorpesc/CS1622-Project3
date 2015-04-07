package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRArrayLength extends IRQuadruple{

  public IRArrayLength(SymbolInfo arg1, SymbolInfo result)
  {
    super("length", arg1, null, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.getName());
      output.append(" := ");
      output.append(_op);
      output.append(" ");
      output.append(_arg1.getName());
      return output.toString();
  }

  public void accept(CodeGenerator g)
  {
    g.visit(this);
  }
}
