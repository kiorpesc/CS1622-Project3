package symboltable;

import java.util.*;

// Concrete implementation of a SymbolTable that allows clients to
// add/remove bindings and scopes (i.e., construct the SymbolTable piecemeal).
public class SymbolTable implements ISymbolTable
{
    private Map<String, ClassSymbol> _classes = new HashMap<String, ClassSymbol>();

    private ClassSymbol _currentClass = null;
    private MethodSymbol _currentMethod = null;

    public void enterClass(String id)
    {
        if (_currentClass != null || _currentMethod != null)
            throw new IllegalStateException("cannot enter class scope while in another class/method scope");

        ClassSymbol classInfo = _classes.get(id);
        if (classInfo == null)
            throw new NoSuchScopeException(id);

        _currentClass = classInfo;
    }

    public void enterMethod(String id)
    {
        if (_currentClass == null)
            throw new IllegalStateException("cannot enter method scope while not in a class scope");

        MethodSymbol method = _currentClass.getMethod(id);
        if (method == null)
            throw new NoSuchScopeException(id);

        _currentMethod = method;
    }

    public void exitMethod()
    {
        if (_currentMethod == null)
            throw new IllegalStateException("cannot exit from method while not in a method`");

        _currentMethod = null;
    }

    public void exitClass()
    {
        if (_currentClass == null)
            throw new IllegalStateException("cannot exit from class while not in a class");

        _currentClass = null;
    }

    public SymbolInfo getSymbol(String id)
    {
        // Check if the current method has a binding for the id
        if (_currentMethod != null)
        {
            VariableSymbol symbol = _currentMethod.getVariable(id);
            if (symbol != null)
                return symbol;
        }

        if (_currentClass != null)
        {
            // Navigate the inheritance hierarchy to see if the binding
            // exists somewhere in current or parent classes.
            ClassSymbol currentClass = _currentClass;
            while (currentClass != null)
            {
                SymbolInfo symbol = _currentClass.getSymbol(id);
                if (symbol != null)
                    return symbol;

                currentClass = _classes.get(currentClass.getParentName());
            }
        }

        // Finally, check if the id corresponds to a class.
        return getClass(id);
    }

    public boolean hasSymbol(String id)
    {
        return getSymbol(id) != null;
    }


    public ClassSymbol getClass(String id)
    {
        return _classes.get(id);
    }

    public boolean hasClass(String id)
    {
        return getClass(id) != null;
    }

    public ClassSymbol getCurrentClass()
    {
        return _currentClass;
    }

    public MethodSymbol getCurrentMethod()
    {
        return _currentMethod;
    }

    // Add a binding for a class
    // Returns the old binding to the identifier, or null if no such binding existed.
    public ClassSymbol addClass(ClassSymbol symbol)
    {
        // TODO: check if id already has a binding in current scope
        return _classes.put(symbol.getName(), symbol);
    }

    // Add a binding for a method to the current class
    public MethodSymbol addMethod(MethodSymbol symbol)
    {
        if (_currentClass == null)
            throw new IllegalStateException("cannot add method outside of a class");

        return _currentClass.addMethod(symbol);
    }

    // Add a binding for a variable to either the current method (if it exists) or the current
    // class.
    public VariableSymbol addVariable(VariableSymbol symbol)
    {
        if (_currentMethod != null)
            return _currentMethod.addLocal(symbol);

        if (_currentClass != null)
            return _currentClass.addVariable(symbol);

        throw new IllegalStateException("cannot add variable outside of class and method scopes");
    }

    public void resolveInheritance()
    {
      for(ClassSymbol cl : _classes.values())
      {
        if(cl.getParentName() != null)
          cl.setParentClass(getClass(cl.getParentName()));
      }
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        for (ClassSymbol currentClass : _classes.values())
            result.append(currentClass.toString());

        return result.toString();
    }
}
