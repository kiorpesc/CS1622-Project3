package irgeneration;

public class IRArrayLookup extends IRQuadruple{

  public String toString()
  {
      StringBuilder output = new StringBuilder(_result.toString());
      output.append(" := ");
      output.append(_arg1);
      output.append("[");
      output.append(_arg2);
      output.append("]");
      return output.toString();
  }

}
