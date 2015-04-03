package irgeneration;

public class IRNewArray extends IRQuadruple{

  public IRNewArray(String op, SymbolInfo arg1, SymbolInfo arg2, SymbolInfo result)
  {
    super(op, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.getName());
      output.append(" := ");
      output.append(_op);
      output.append(" ");
      output.append(_arg1.getName());
      output.append(", ");
      output.append(_arg2.getName());
      return output.toString();
  }

}
