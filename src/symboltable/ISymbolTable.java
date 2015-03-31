package symboltable;

public interface ISymbolTable
{
    // Enter a class.
    public void enterClass(String id);
    
    // Exit a class.
    public void exitClass();
    
    // Enter a method.
    public void enterMethod(String id);
    
    // Exit a method.
    public void exitMethod();

    // Get the SymbolInfo for the symbol in the current scope
    public SymbolInfo getSymbol(String id);

    // Returns true iff there's a binding for the given symbol in the current scope.
    public boolean hasSymbol(String id);

    // Return the ClassSymbol for the given id
    public ClassSymbol getClass(String id);

    // Return true iff there's a class with the given id 
    public boolean hasClass(String id);

    // Returns the current ClassSymbol
    public ClassSymbol getCurrentClass();

    // Returns the current MethodSymbol
    public MethodSymbol getCurrentMethod();

}
