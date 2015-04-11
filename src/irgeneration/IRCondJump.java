package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;

public class IRCondJump extends IRQuadruple{

  private String _ifFalse;
  private String _label;

  public IRCondJump(SymbolInfo arg1, String label)
  {
    super("goto", arg1, null, null);
    _ifFalse = "iffalse";
    _label = label;
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_ifFalse);
      output.append(" ");
      output.append(_arg1.getName());
      output.append(" ");
      output.append(_op);
      output.append(" ");
      output.append(_label);
      return output.toString();
  }

  public String getLabel()
  {
    return _label;
  }

  public void accept(CodeGenerator g)
  {
    g.visit(this);
  }
}
