package objectimpl;

import java.util.*;

import symboltable.*;

public class ObjectLayoutManager
{
    private ISymbolTable _symbolTable;

    private Map<SymbolInfo, Integer> _variableOffsets;
    private Map<SymbolInfo, List<VariableSymbol>> _classesToVariables;

    public ObjectLayoutManager(Collection<ClassSymbol> classes)
    {
        _classesToVariables = new HashMap<SymbolInfo, List<VariableSymbol>>();
        _variableOffsets = new HashMap<SymbolInfo, Integer>();

        for (ClassSymbol classSym : classes)
            computeOffsets(classSym);
    }

    public boolean isInstanceVariable(SymbolInfo sym)
    {
        return _variableOffsets.containsKey(sym);
    }

    public int getSizeInBytes(SymbolInfo classSym)
    {
        return _classesToVariables.get(classSym).size() * 4;
    }

    public int getByteOffset(SymbolInfo sym)
    {
        return _variableOffsets.get(sym);
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        for (SymbolInfo classSym : _classesToVariables.keySet())
        {
            result.append(classSym.getName());
            result.append(" {\n");
            List<VariableSymbol> vars = _classesToVariables.get(classSym);
            for (VariableSymbol varSym : vars)
            {
                result.append(varSym);
                result.append(" : offset ");
                result.append(_variableOffsets.get(varSym));
                result.append("\n");
            }
            result.append("}\n\n");
        }
        return result.toString();
    }

    private void computeOffsets(ClassSymbol current)
    {
        List<VariableSymbol> variables = _classesToVariables.get(current);

        if (variables == null)
        {
            // we have no mapping, so gather our variables (including parent's).
            variables = new ArrayList<VariableSymbol>();
            _classesToVariables.put(current, variables);

            ClassSymbol parent = current.getParentClass();
            // see if we have a class to extend
            if (parent != null)
            {
                // compute the offsets of our parent first
                if (!_classesToVariables.containsKey(parent))
                    computeOffsets(parent);

                variables.addAll(_classesToVariables.get(parent));
            }

            // now add our variables
            for (VariableSymbol var : current.getVariables())
                variables.add(var);
        }
        // now we can add our variable offsets
        for (int i = 0; i < variables.size(); ++i)
        {
            VariableSymbol sym = variables.get(i);
            // multiply index by 4, since each class member
            // will be 4 bytes
            _variableOffsets.put(sym, i * 4);
        }
    }
}