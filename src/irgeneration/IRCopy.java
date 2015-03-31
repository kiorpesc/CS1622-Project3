package irgeneration;

public class IRCopy extends IRQuadruple{

  public IRCopy(String op, String arg1, String arg2, String result)
  {
    super(op, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result);
      output.append(" := ");
      output.append(_arg1);
      return output.toString();
  }

}
