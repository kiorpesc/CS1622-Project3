package symboltable;

import visitor.Visitor;
import syntaxtree.*;

import java.util.*;

// Visitor implementation that builds a SymbolTable
// Also recognizes redefinition errors
public class BuildSymbolTableVisitor implements Visitor
{
    private SymbolTable _symbolTable = new SymbolTable();
    private List<String> _errors = new ArrayList<String>();

    // Returns the SymbolTable.
    public ISymbolTable getSymbolTable()
    {
        return _symbolTable;
    }

    // Returns a list of redefinition errors encountered.
    public List<String> getErrors()
    {
        return _errors;
    }

    // Attempt to enter a child scope from the current scope in the SymbolTable.
    private void enterScope(String s)
    {
        // Just print out the error message for now. 
        // TODO: figure out how we should handle the errors (see project spec)
        try
        {
            _symbolTable.enterScope(s);
        }
        catch (UnknownSymbolException e)
        {
            e.printStackTrace();
        }
    }

    // Add a binding between the given symbol and its info, recordining
    // a redefinition error if necessary.
    private void addBinding(String name, SymbolInfo info)
    {
        SymbolInfo old = _symbolTable.addBinding(name, info);
        if (old != null)
            _errors.add("Multiply defined identifier " + name);
    }

    public void visit(Program n)
    {
        n.m.accept(this);

        ClassDeclList list = n.cl;
        for (int i = 0; i < list.size(); ++i)
        {
            list.elementAt(i).accept(this);
        }
    }

    public void visit(MainClass n)
    {
        // Create scope for the class
        String name = n.i1.s;
        _symbolTable.addScope(name);
        // Enter that scope
        enterScope(name);
        
        // Create scope for main method
        _symbolTable.addScope("main");
        enterScope("main");
        // Create binding for main method argument name
        addBinding(n.i2.s, new SymbolInfo(n.i2.s));
        _symbolTable.exitScope();

        // add binding for main method
        addBinding("main", new SymbolInfo("main"));

        // Back to root scope
        _symbolTable.exitScope();

        // Add a binding for the mainclass
        addBinding(name, new SymbolInfo(name));
    }
    public void visit(ClassDeclSimple n)
    {
        // Create scope for the class
        String name = n.i.s;
        _symbolTable.addScope(name);
        enterScope(name);

        // visit variable declarations
        for (int i = 0; i < n.vl.size(); ++i)        
            n.vl.elementAt(i).accept(this);

        // visit method declarations
        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        // Add SymbolInfo
        _symbolTable.exitScope();
        // TODO: record method/variable names,
        addBinding(name, new SymbolInfo(name));


    }
    public void visit(ClassDeclExtends n)
    {
        // Create scope for the class
        String name = n.i.s;
        _symbolTable.addScope(name);
        enterScope(name);

        // visit variable declarations
        for (int i = 0; i < n.vl.size(); ++i)        
            n.vl.elementAt(i).accept(this);

        // visit method declarations
        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        // Add SymbolInfo
        _symbolTable.exitScope();
        // TODO: record method/variable names, extended type
        addBinding(name, new SymbolInfo(name));
    }
    public void visit(VarDecl n)
    {
        // TODO: add Type to SymbolInfo
        addBinding(n.i.s, new SymbolInfo(n.i.s));
    }
    public void visit(MethodDecl n)
    {
        // Create a scope for the method
        String name = n.i.s;
        _symbolTable.addScope(name);
        enterScope(name);

        // visit formal declarations
        for (int i = 0; i < n.fl.size(); ++i)
            n.fl.elementAt(i).accept(this);

        // visit local declarations
        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);

        // Exit scope and add info for this method declaration
        _symbolTable.exitScope();
        addBinding(name, new SymbolInfo(name));
    }
    public void visit(Formal n)
    {
        // TODO: record type
        addBinding(n.i.s, new SymbolInfo(n.i.s));
    }
    public void visit(IntArrayType n) { }
    public void visit(BooleanType n) { }
    public void visit(IntegerType n) { }
    public void visit(IdentifierType n) { }
    public void visit(Block n) { }
    public void visit(If n) { }
    public void visit(While n) { }
    public void visit(Print n) { }
    public void visit(Assign n) { }
    public void visit(ArrayAssign n) { }
    public void visit(And n) { }
    public void visit(LessThan n) { }
    public void visit(Plus n) { }
    public void visit(Minus n) { }
    public void visit(Times n) { }
    public void visit(ArrayLookup n) { }
    public void visit(ArrayLength n) { }
    public void visit(Call n) { }
    public void visit(IntegerLiteral n) { }
    public void visit(True n) { }
    public void visit(False n) { }
    public void visit(IdentifierExp n) { }
    public void visit(This n) { }
    public void visit(NewArray n) { }
    public void visit(NewObject n) { }
    public void visit(Not n) { }
    public void visit(Identifier n) { }
}