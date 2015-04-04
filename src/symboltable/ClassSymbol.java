package symboltable;

import java.util.*;

// Holds information relevant for a ClassSymbol
public class ClassSymbol extends SymbolInfo
{
    private Map<String, MethodSymbol> _methods = new HashMap<String, MethodSymbol>();
    private Map<String, VariableSymbol> _variables = new HashMap<String, VariableSymbol>();
    private String _parentName;
    private ClassSymbol _parentClass;

    public ClassSymbol(String name)
    {
        this(name, null);
    }

    public ClassSymbol(String name, String parent)
    {
        super(name);
        _parentName = parent;
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
        return "class";
    }

    // Retrieve the method specified by id
    public MethodSymbol getMethod(String id)
    {
        if(!_methods.containsKey(id) && _parentClass != null)
          return _parentClass.getMethod(id);
        return _methods.get(id);
    }

    // Add a method to this class, returning the old MethodSymbol mapped to the
    // name if it exists (null otherwise).
    public MethodSymbol addMethod(MethodSymbol symbol)
    {
        MethodSymbol old = _methods.get(symbol.getName());
        _methods.put(symbol.getName(), symbol);
        return old;
    }

    // Retrieve the variable specified by id
    public VariableSymbol getVariable(String id)
    {
        if(!_variables.containsKey(id) && _parentClass != null)
          return _parentClass.getVariable(id);
        return _variables.get(id);
    }

    // Return the Symbol specified by id
    public SymbolInfo getSymbol(String id)
    {
        SymbolInfo symbol = getMethod(id);

        return (symbol != null) ? symbol : getVariable(id);
    }

    // Add a variable to this class, returning the old VariableSymbol mapped to the
    // name if it exists (null otherwise).
    public VariableSymbol addVariable(VariableSymbol symbol)
    {
        VariableSymbol old = _variables.get(symbol.getName());
        _variables.put(symbol.getName(), symbol);
        return old;
    }

    // Returns the name of the parent class.
    public String getParentName()
    {
        return _parentName;
    }

    public void setParentClass(ClassSymbol cl)
    {
      _parentClass = cl;
    }

    public ClassSymbol getParentClass()
    {
      return _parentClass;
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

        result.append("\n");

        for (MethodSymbol method : _methods.values())
            result.append(method.toString());

        result.append("}\n");
        return result.toString();
    }
}
