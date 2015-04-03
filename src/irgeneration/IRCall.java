package irgeneration;

public class IRCall extends IRQuadruple{

  private int _numParams;

  public IRCall(SymbolInfo arg1, int numParams, SymbolInfo result)
  {
    super("call", arg1, null, result);
    _numParams = numParams;
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.getName());
      output.append(" := ");
      output.append(_op);
      output.append(" ");
      output.append(_arg1.getName());
      output.append(" ");
      output.append(_numParams);
      return output.toString();
  }

}
