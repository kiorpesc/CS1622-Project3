package symboltable;

public abstract class SymbolInfo
{
    private String _name;

    public SymbolInfo(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;        
    }

    public abstract boolean isLValue();
    public abstract boolean isRValue();
    public abstract String getSymbolType();    
}