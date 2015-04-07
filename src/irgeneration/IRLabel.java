package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRLabel extends IRQuadruple{

  String _label;

  public IRLabel(String label)
  {
    super(null, null, null, null); // gross
    _label = label;
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_label);
      output.append(":");
      return output.toString();
  }

  public void accept(CodeGenerator g)
  {
    g.visit(this);
  }
}
