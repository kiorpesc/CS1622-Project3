package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRUncondJump extends IRQuadruple{

  private String _label;

  public IRUncondJump(String label)
  {
    super("goto", null, null, null);
    _label = label;
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder();
      output.append(_op);
      output.append(" ");
      output.append(_label);
      return output.toString();
  }

  public String getLabel()
  {
    return _label;
  }

  public void accept(IRVisitor g)
  {
    g.visit(this);
  }
}
