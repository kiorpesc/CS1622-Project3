package irgeneration;

public class IRArrayLength extends IRQuadruple{

  public IRArrayLength(String op, String arg1, String arg2, String result)
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
      return output.toString();
  }

}
