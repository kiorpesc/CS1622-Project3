package irgeneration;

public class IRArrayAssign extends IRQuadruple{

  public IRArrayAssign(String op, SymbolInfo arg1, SymbolInfo arg2, SymbolInfo result)
  {
    super(op, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_arg1.getName());
      output.append("[");
      output.append(_arg2.getName());
      output.append("]");
      output.append(" := ");
      output.append(_result.getName());
      return output.toString();
  }

}
