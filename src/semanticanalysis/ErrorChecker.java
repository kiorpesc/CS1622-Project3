package semanticanalysis;

import java.util.*;

public abstract class ErrorChecker
{
    private List<String> _errors = new ArrayList<String>();

    public List<String> getErrors()
    {
        return _errors;
    }

    public boolean hasErrors()
    {
        return !_errors.isEmpty();
    }

    protected void addError(String error, int line, int column)
    {
        _errors.add(formatError(error, line, column));
    }

    private String formatError(String error, int line, int column)
    {
        StringBuilder result = new StringBuilder(error);
        result.append(" at line ");
        result.append(line);
        result.append(", character ");
        result.append(column);
        return result.toString();
    } 
}
