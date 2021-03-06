package semanticanalysis;

import visitor.Visitor;
import symboltable.*;
import syntaxtree.*;

import java.util.*;

// Visitor implementation that builds a SymbolTable
// Also recognizes redefinition errors
public class BuildSymbolTableVisitor extends ErrorChecker implements Visitor
{
    private SymbolTable _symbolTable = new SymbolTable();

    // Returns the SymbolTable.
    public ISymbolTable getSymbolTable()
    {
        return _symbolTable;
    }

    // Records an error if a duplicate symbol is detected
    private void checkDuplicate(SymbolInfo oldSymbol, int line, int col)
    {
        if (oldSymbol != null)
            addError("Multiply defined identifier " + oldSymbol.getName(), line, col);
    }

    public void visit(Program n)
    {
        n.m.accept(this);

        ClassDeclList list = n.cl;

        for (int i = 0; i < list.size(); ++i)
            list.elementAt(i).accept(this);

        _symbolTable.resolveInheritance();
    }

    public void visit(MainClass n)
    {
        // Create scope for the class
        String name = n.i1.s;
        checkDuplicate(_symbolTable.addClass(new ClassSymbol(name)), n.getLine(), n.getColumn());

        // Enter that scope
        _symbolTable.enterClass(name);

        // Add binding for main method
        checkDuplicate(_symbolTable.addMethod(new MethodSymbol("main", null)), n.getLine(), n.getColumn());

        // exit the class
        _symbolTable.exitClass();

    }
    public void visit(ClassDeclSimple n)
    {
        // Create scope for the class
        String name = n.i.s;
        checkDuplicate(_symbolTable.addClass(new ClassSymbol(name)), n.getLine(), n.getColumn());
        _symbolTable.enterClass(name);

        // visit variable declarations
        if (n.vl != null)
        {
            for (int i = 0; i < n.vl.size(); ++i)
                n.vl.elementAt(i).accept(this);
        }

        // visit method declarations
        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        // Add Symbol
        _symbolTable.exitClass();
    }
    public void visit(ClassDeclExtends n)
    {
        // Create scope for the class
        String name = n.i.s;
        checkDuplicate(_symbolTable.addClass(new ClassSymbol(name, n.j.s)), n.getLine(), n.getColumn());
        _symbolTable.enterClass(name);

        // visit variable declarations
        if (n.vl != null)
        {
            for (int i = 0; i < n.vl.size(); ++i)
                n.vl.elementAt(i).accept(this);
        }

        // visit method declarations
        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        _symbolTable.exitClass();
    }
    public void visit(VarDecl n)
    {
        // TODO: add Type to Symbol
        checkDuplicate(_symbolTable.addVariable(new VariableSymbol(n.i.s, n.t)), n.getLine(), n.getColumn());
    }
    public void visit(MethodDecl n)
    {
        // Create a scope for the method
        String name = n.i.s;
        checkDuplicate(_symbolTable.addMethod(new MethodSymbol(name, n.t)), n.getLine(), n.getColumn());
        _symbolTable.enterMethod(name);

        // TODO: do we need to add symbol for 'this'?

        // visit formal declarations
        if (n.fl != null)
        {
            for (int i = 0; i < n.fl.size(); ++i)
                n.fl.elementAt(i).accept(this);
        }
        // visit local declarations
        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);

        // Exit scope and add info for this method declaration
        _symbolTable.exitMethod();
    }
    public void visit(Formal n)
    {
        checkDuplicate(_symbolTable.getCurrentMethod().addFormal(new VariableSymbol(n.i.s, n.t)), n.getLine(), n.getColumn());
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
