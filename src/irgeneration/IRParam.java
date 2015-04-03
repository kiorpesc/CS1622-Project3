package irgeneration;

public class IRParam extends IRQuadruple{

  public IRParam(SymbolInfo arg1)
  {
    super("param", arg1, null, null);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_op);
      output.append(" ");
      output.append(_arg1.getName());
      return output.toString();
  }

}
