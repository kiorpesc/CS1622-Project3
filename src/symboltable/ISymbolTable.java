package symboltable;

public interface ISymbolTable
{
    // Enter a new scope.
    public void enterScope(String id) throws UnknownSymbolException;

    // Exit the current scope to the parent scope.
    public void exitScope();

    // Get the SymbolInfo for the given name in the current scope.
    public SymbolInfo getSymbol(String id) throws UnknownSymbolException;
}
