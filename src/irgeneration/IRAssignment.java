package irgeneration;

public class IRAssignment extends IRQuadruple{

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.toString());
      output.append(" := ");
      output.append(_arg1);
      output.append(" ");
      output.append(_op);
      output.append(" ");
      output.append(_arg2);
      return output.toString();
  }

}
