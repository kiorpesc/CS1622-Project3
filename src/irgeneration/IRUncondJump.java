package irgeneration;

public class IRUncondJump extends IRQuadruple{

  public IRUncondJump(String op, String arg1, String arg2, String result)
  {
    super(op, arg1, arg2, result);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder();
      output.append(_op);
      output.append(" ");
      output.append(_arg1);
      return output.toString();
  }

}
