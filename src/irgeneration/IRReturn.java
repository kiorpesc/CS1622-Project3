package irgeneration;

public class IRReturn extends IRQuadruple{

  public IRReturn(String op, SymbolInfo arg1, SymbolInfo arg2, SymbolInfo result)
  {
    super(op, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_op);
      output.append(" ");
      output.append(_arg1.getName());
      return output.toString();
  }

}
