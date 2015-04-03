package irgeneration;

// test symbol, please ignore
import symboltable.*;
import syntaxtree.*;

public class ConstantSymbol extends VariableSymbol
{

  public ConstantSymbol (String name, Type type)
  {
    super(name, type);
  }

  public String getSymbolType()
  {
      return "constant";
  }

  public String getValue()
  {
    return getName();
  }

}
