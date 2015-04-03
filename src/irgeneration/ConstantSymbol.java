// test symbol, please ignore

public class ConstantSymbol extends VariableSymbol
{

  public ConstantSymbol (String name, Type type)
  {
    super(name, type);
    _value = value;
  }

  public String getSymbolType()
  {
      return "constant";
  }

  public String getValue()
  {
    return _name;
  }

}
