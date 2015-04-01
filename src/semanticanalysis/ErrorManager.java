package semanticanalysis;

import java.util.*;

public class ErrorManager
{
    private List<String> _errors = new ArrayList<String>();

    public List<String> getErrors()
    {
        return _errors;
    }

    public void addError(String error, int line, int column)
    {
        _errors.add(formatError(error, line, column));
    }

    private String formatError(String error, int line, int column)
    {
        StringBuilder result = new StringBuilder(error);
        result.append(" at ");
        result.append(line);
        result.append(", character ");
        result.append(column);
        return result.toString();
    } 
}