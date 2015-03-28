package symboltable;

public class SymbolTable implements ISymbolTable
{
    private Environment _root; 
    private Environment _current;

    public SymbolTable()
    {
        _root = new Environment();
        _current = _root;
    }

    public void enterScope(String id) throws UnknownSymbolException
    {
        Environment env = _current.getChild(id);
        if (env == null)
            throw new UnknownSymbolException(id);


        _current = env;
    }

    public void exitScope()
    {
        Environment env = _current.getParent();
        if (env == null)
            throw new IllegalStateException("cannot exit from root scope");

        _current = env;
    }

    public SymbolInfo getSymbol(String id) throws UnknownSymbolException
    {        
        Environment current = _current;
        SymbolInfo result = null;

        // Traverse upward through scopes to attempt to find the symbol.
        while (result == null && current != null)
        {
            result = current.getSymbol(id);
            current = current.getParent();
        }

        if (result == null)
            throw new UnknownSymbolException(id);

        return result;
    }

    // Add a binding for a name
    // Returns the old binding to the identifier, or null if no such binding existed.
    public SymbolInfo addBinding(String id, SymbolInfo info)
    {
        // TODO: check if id already has a binding in current scope
        return _current.addSymbol(id, info);
    }

    // Add an Environment for a name
    public void addScope(String id)
    {
        // TODO: check if id already has environment in current scope
        _current.addChild(id, new Environment(_current));
    }

    public String toString()
    {
        return toString(_root, 0);
    }

    private static String toString(Environment current, int level)
    {
        StringBuilder result = new StringBuilder();
        result.append("{\n");

        for (String name : current.getSymbols())
        {
            addTabs(result, level + 1);
            result.append(name); 
            result.append("\n");       
        }            
        
        for (String name : current.getChildren())
        {
            addTabs(result, level + 1);
            result.append(name);
            result.append(' ');
            result.append(toString(current.getChild(name), level + 1));
        }

        addTabs(result, level);
        result.append("}\n");
        return result.toString();
    }

    private static void addTabs(StringBuilder sb, int num)
    {
        for (int i = 0; i < num; ++i)
            sb.append('\t');
    }


}