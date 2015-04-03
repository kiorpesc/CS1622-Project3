package irgeneration;

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
      StringBuilder output = new StringBuilder(_result.getName());
      output.append(" := ");
      output.append(_arg1.getName());
      output.append(" ");
      output.append(_op);
      output.append(" ");
      output.append(_arg2.getName());
      return output.toString();
  }

}
