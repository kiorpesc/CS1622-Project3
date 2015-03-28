package symboltable;

import java.util.*;

public class Environment
{
    private Map<String, Environment> _children;
    private Map<String, SymbolInfo> _symbols;
    private Environment _parent;

    public Environment()
    {
        this(null);
    }

    public Environment(Environment parent)
    {
        _children = new HashMap<String, Environment>();
        _symbols = new HashMap<String, SymbolInfo>();

        _parent = parent;
    }

    public Environment getParent()
    {
        return _parent;
    }

    public Environment getChild(String id)
    {
        return _children.get(id);
    }

    // Returns the previous Environment mapped to this id, or null if one did not exist.
    public Environment addChild(String id, Environment env)
    {
        if (id == null || env == null)
            throw new IllegalArgumentException("id and env must not be null");

        return _children.put(id, env);
    }
    
    public SymbolInfo getSymbol(String id)
    {
        return _symbols.get(id);
    }  

    // Returns the previous SymbolInfo mapped to this id, or null if one did not exist.
    public SymbolInfo addSymbol(String id, SymbolInfo info)
    {
        if (id == null || info == null)
            throw new IllegalArgumentException("id and info must not be null");

        return _symbols.put(id, info);
    }

    public Collection<String> getChildren()
    {
        return _children.keySet();
    }

    public Collection<String> getSymbols()
    {
        return _symbols.keySet();
    }
}