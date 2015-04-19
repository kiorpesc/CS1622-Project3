package symboltable;

import syntaxtree.Type;

import java.util.*;

// Holds information relevant to a Method symbol.
public class MethodSymbol extends SymbolInfo
{
    private Map<String, VariableSymbol> _formals = new LinkedHashMap<String, VariableSymbol>();
    private Map<String, VariableSymbol> _locals = new HashMap<String, VariableSymbol>();
    private String _label;
    private Type _returnType;

    public MethodSymbol(String name, Type returnType)
    {
        super(name);
        _returnType = returnType;
    }

    public boolean isLValue()
    {
        return false;
    }

    public boolean isRValue()
    {
        return false;
    }

    public String getSymbolType()
    {
        return "method";
    }

    public ArrayList<String> getFormalNames()
    {
      ArrayList<String> result = new ArrayList<String>();

      for(VariableSymbol var : _formals.values())
      {
        result.add(var.getName());
      }

      return result;
    }

    public List<Type> getFormalTypes()
    {
        List<Type> result = new ArrayList<Type>();

        for (String key : _formals.keySet())
            result.add(_formals.get(key).getType());

        return result;
    }

    public Collection<VariableSymbol> getFormalSymbols()
    {
      return _formals.values();
    }

    public Collection<VariableSymbol> getLocalSymbols()
    {
      return _locals.values();
    }

    public Type getReturnType()
    {
        return _returnType;
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

    public VariableSymbol getVariable(String id)
    {
        if (_formals.containsKey(id))
            return _formals.get(id);
        return _locals.get(id);
    }

    // Returns true iff a variable with the given name exists in this method's scope.
    public boolean hasVariable(String id)
    {
        return getVariable(id) != null;
    }

    public void setLabel(String label)
    {
      _label = label;
    }

    public String getLabel()
    {
      return _label;
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
        if (_returnType != null)
            result.append(_returnType.getName());
        result.append(" {\n");

        for (VariableSymbol local : _locals.values())
            result.append(local.toString());

        result.append("}\n");
        return result.toString();
    }
}
