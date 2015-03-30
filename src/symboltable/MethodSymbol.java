package symboltable;

import java.util.*;

public class MethodSymbol extends SymbolInfo 
{
    private Map<String, VariableSymbol> _formals = new HashMap<String, VariableSymbol>();
    private Map<String, VariableSymbol> _locals = new HashMap<String, VariableSymbol>();

    public MethodSymbol(String name)
    {
        super(name);
    }

    // Adds a VariableSymbol to the formal list, returning the old
    // VariableSymbol mapped to the same name (if it exists), null otherwise
    public VariableSymbol addFormal(VariableSymbol formal)
    {
        String name = formal.getName();
        VariableSymbol old = _formals.put(name, formal);
        
        // Check if a local variable with the same name exists
        if (old == null)
            old = _locals.get(name);

        return old;
    }

    // Adds a VariableSymbol to the local list, returning the old
    // VariableSymbol mapped to the same name (if it exists), null otherwise
    public VariableSymbol addLocal(VariableSymbol symbol)
    {
        String name = symbol.getName();
        VariableSymbol old = _locals.put(name, symbol);
        
        // Check if a formal variable with the same name exists
        if (old == null)
            old = _formals.get(name);

        return old;
    }

    public boolean hasVariable(String id)
    {
        return _formals.containsKey(id) || _locals.containsKey(id);
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        result.append(getName());
        if (_formals.isEmpty())
        {
            result.append("()");
        }
        else
        {
            result.append('(');
            for (VariableSymbol formal : _formals.values())
            {
                result.append(formal.toString());
                result.append(',');
            }

            // remove extraneous comma
            result.setCharAt(result.length() - 1, ')');
        }
        
        result.append(" : ");
        // TODO: return type
        result.append("{\n");

        for (VariableSymbol local : _locals.values())
        {
            result.append(local.toString());
            result.append("\n");
        }

        result.append("}\n");
        return result.toString();
    }
}