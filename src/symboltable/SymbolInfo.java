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
}