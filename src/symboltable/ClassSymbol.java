package symboltable;

import java.util.*;

public class ClassSymbol extends SymbolInfo 
{
    private Map<String, MethodSymbol> _methods = new HashMap<String, MethodSymbol>();
    private Map<String, VariableSymbol> _variables = new HashMap<String, VariableSymbol>();
    private String _parentName;

    public ClassSymbol(String name)
    {
        this(name, null);
    }

    public ClassSymbol(String name, String parent)
    {
        super(name);
        _parentName = parent;
    }

    public MethodSymbol getMethod(String id)
    {
        return _methods.get(id);
    }

    public MethodSymbol addMethod(MethodSymbol symbol)
    {
        MethodSymbol old = _methods.get(symbol.getName());
        _methods.put(symbol.getName(), symbol);
        return old;
    }

    public VariableSymbol getVariable(String id)
    {
        return _variables.get(id);
    }

    public VariableSymbol addVariable(VariableSymbol symbol)
    {
        VariableSymbol old = _variables.get(symbol.getName());
        _variables.put(symbol.getName(), symbol);
        return old;
    }

    public boolean hasBinding(String id)
    {
        return _methods.containsKey(id) || _variables.containsKey(id);
    }

    public String getParentName()
    {
        return _parentName;
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        result.append(getName());
        result.append(" : ");
        result.append(getParentName());
        result.append(" {\n");

        for (VariableSymbol variable : _variables.values())
            result.append(variable.toString());

        for (MethodSymbol method : _methods.values())
            result.append(method.toString());

        result.append("}\n");
        return result.toString();        
    }
}