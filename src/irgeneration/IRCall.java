package irgeneration;

import symboltable.*;
import codegen.CodeGenerator;

public class IRCall extends IRQuadruple{

  private int _numParams;
  private String _label;

  public IRCall(MethodSymbol arg1, int numParams, SymbolInfo result)
  {
    super("call", arg1, null, result);
    _numParams = numParams;
    _label = arg1.getLabel();
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder();
      if(_result != null)
      {
        output.append(_result.getName());
        output.append(" := ");
      }
      output.append(_op);
      output.append(" ");
      output.append(_arg1.getName());
      output.append(" ");
      output.append(_numParams);
      return output.toString();
  }

  public void accept(IRVisitor g)
  {
    g.visit(this);
  }
}
