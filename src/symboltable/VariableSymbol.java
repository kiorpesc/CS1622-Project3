package symboltable;

import syntaxtree.Type;

public class VariableSymbol extends SymbolInfo 
{
    private Type _type;

    public VariableSymbol(String name, Type t)
    {
        super(name);
        _type = t;
    }

    public Type getType()
    {
        return _type;
    }

    public String toString()
    {
        return getName() + " : " + _type + "\n";
    }
}