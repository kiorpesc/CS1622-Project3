package symboltable;

public interface ISymbolTable
{
    // Enter a new scope.
    public void enterScope(String id);

    // Exit the current scope to the parent scope.
    public void exitScope();
    
    // Get the SymbolInfo for the given name in the current scope.
    public SymbolInfo getSymbol(String id) throws UnknownSymbolException;

    // Returns true iff there's a binding for the given symbol in the current scope.
    public boolean hasSymbol(String id);
}
