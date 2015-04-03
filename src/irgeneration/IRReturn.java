package irgeneration;

import symboltable.SymbolInfo;

public class IRReturn extends IRQuadruple{

  public IRReturn(SymbolInfo arg1)
  {
    super("return", arg1, null, null);
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_op);
      output.append(" ");
      output.append(_arg1.getName());
      return output.toString();
  }

}
