package irgeneration;

import symboltable.SymbolInfo;

public class IRLabel extends IRQuadruple{

  String _label;

  public IRLabel(String label)
  {
    super(null, null, null, null); // gross
    _label = label;
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_label);
      output.append(":");
      return output.toString();
  }

}
