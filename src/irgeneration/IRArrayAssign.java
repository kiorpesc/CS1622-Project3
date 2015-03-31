package irgeneration;

public class IRArrayAssign extends IRQuadruple{

  public IRArrayAssign(String op, String arg1, String arg2, String result)
  {
    super(op, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_arg1);
      output.append("[");
      output.append(_arg2);
      output.append("]");
      output.append(" := ");
      output.append(_result);
      return output.toString();
  }

}
