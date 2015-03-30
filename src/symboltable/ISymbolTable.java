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

    // Returns true iff there's a binding for the given symbol in the current scope.
    public boolean hasSymbol(String id);
}
