package irgeneration;

import symboltable.SymbolInfo;
import codegen.CodeGenerator;
import symboltable.MethodSymbol;

public class IRLabel extends IRQuadruple{

  private String _label;
  private MethodSymbol _method;
  private boolean _isMethod;

  public IRLabel(String label)
  {
    super(null, null, null, null); // gross
    _label = label;
  }

  public IRLabel(String label, MethodSymbol method)
  {
    super(null, null, null, null); // gross
    _label = label;
    _method = method;
    _isMethod = true;
  }

  public String toString()
  {
      StringBuilder output = new StringBuilder(_label);
      output.append(":");
      return output.toString();
  }

  public void accept(IRVisitor g)
  {
    g.visit(this);
  }

  public boolean isMethod()
  {
    return _isMethod;
  }

  public MethodSymbol getMethod()
  {
    return _method;
  }

  public String getLabel()
  {
    return _label;
  }
}
