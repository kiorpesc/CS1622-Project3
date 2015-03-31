package irgeneration;

public class IRNewArray extends IRQuadruple{

  public IRNewArray(String op, String arg1, String arg2, String result)
  {
    super(op, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.toString());
      output.append(" := ");
      output.append(_op);
      output.append(" ");
      output.append(_arg1);
      output.append(", ");
      output.append(_arg2);
      return output.toString();
  }

}
