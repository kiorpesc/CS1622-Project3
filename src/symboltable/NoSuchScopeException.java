package symboltable;

public class NoSuchScopeException extends RuntimeException
{
    public NoSuchScopeException(String id)
    {
        super("No scope exists for " + id);
    }
}