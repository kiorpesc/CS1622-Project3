package semanticanalysis;

import visitor.Visitor;
import symboltable.*;
import syntaxtree.*;

import java.util.*;

// Check the AST for usage of undefined names
public class NameAnalysisVisitor extends ErrorChecker implements Visitor
{
    private ISymbolTable _symbolTable;

    public NameAnalysisVisitor(ISymbolTable symbolTable)
    {
        _symbolTable = symbolTable;
    }

    // Records an unknown symbol error.
    private void recordUnknownSymbolError(String name, int line, int col)
    {
        addError("Use of undefined identifier " + name, line, col);
    }

    public void visit(Program n)
    {
        n.m.accept(this);

        ClassDeclList list = n.cl;
        for (int i = 0; i < list.size(); ++i)
        {
            ClassDecl next = list.elementAt(i);
            if (next != null)
                next.accept(this);
        }
    }
    public void visit(MainClass n)
    {
        String name = n.i1.s;

        _symbolTable.enterClass(name);
        _symbolTable.enterMethod("main");

        if (n.s != null)
            n.s.accept(this);

        _symbolTable.exitMethod();
        _symbolTable.exitClass();

    }
    public void visit(ClassDeclSimple n)
    {
        String name = n.i.s;

        _symbolTable.enterClass(name);
        
        // visit VarDecls
        if (n.vl != null)
        {
            for (int i = 0; i < n.vl.size(); ++i)
            {
                VarDecl next = n.vl.elementAt(i);
                if (next != null)
                    next.accept(this);
            }
        }

        // visit methods
        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        _symbolTable.exitClass();
    }
    public void visit(ClassDeclExtends n)
    {
        String name = n.i.s;

        _symbolTable.enterClass(name);

        // visit VarDecls
        if (n.vl != null)
        {
            for (int i = 0; i < n.vl.size(); ++i)
            {
                VarDecl next = n.vl.elementAt(i);
                if (next != null)
                    next.accept(this);
            }
        }

        // visit methods
        for (int i = 0; i < n.ml.size(); ++i)
        {
            MethodDecl next = n.ml.elementAt(i);
            if (next != null)                
                next.accept(this);
        }

        _symbolTable.exitClass();
    }
    public void visit(VarDecl n) 
    { 
        n.t.accept(this);
    }
    public void visit(MethodDecl n)
    {
        String name = n.i.s;

        _symbolTable.enterMethod(name);
        
        // check formals
        if (n.fl != null)
        {
            for (int i = 0; i < n.fl.size(); ++i)
                n.fl.elementAt(i).accept(this);
        }

        // check vardecls
        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);

        // visit statements
        for (int i = 0; i < n.sl.size(); ++i)
        {
            Statement next = n.sl.elementAt(i);
            if (next != null)
                next.accept(this);
        }

        // visit return expression
        n.e.accept(this);

        _symbolTable.exitMethod();
    }
    public void visit(Formal n) 
    { 
        // check its type 
        n.t.accept(this);
    }
    public void visit(IntArrayType n) { }
    public void visit(BooleanType n) { }
    public void visit(IntegerType n) { }
    public void visit(IdentifierType n)
    {
        if (!_symbolTable.hasClass(n.s))
            recordUnknownSymbolError(n.s, n.getLine(), n.getColumn());
    }
    public void visit(Block n)
    {
        // visit statements
        for (int i = 0; i < n.sl.size(); ++i)
            n.sl.elementAt(i).accept(this);
    }
    public void visit(If n)
    {
        n.e.accept(this);

        if (n.s1 != null)
            n.s1.accept(this);
        if (n.s2 != null)
            n.s2.accept(this);
    }
    public void visit(While n)
    {
        n.e.accept(this);
        if (n.s != null)
            n.s.accept(this);
    }
    public void visit(Print n)
    {
        n.e.accept(this);
    }
    public void visit(Assign n)
    {
        if (!_symbolTable.hasSymbol(n.i.s))
            recordUnknownSymbolError(n.i.s, n.getLine(), n.getColumn());

        n.e.accept(this);
    }
    public void visit(ArrayAssign n)
    {
        if (!_symbolTable.hasSymbol(n.i.s))
            recordUnknownSymbolError(n.i.s, n.getLine(), n.getColumn());

        n.e1.accept(this);
        n.e2.accept(this);
    }
    public void visit(And n)
    {
        n.e1.accept(this);
        n.e2.accept(this);
    }
    public void visit(LessThan n)
    {
        n.e1.accept(this);
        n.e2.accept(this);
    }
    public void visit(Plus n)
    {
        n.e1.accept(this);
        n.e2.accept(this);
    }
    public void visit(Minus n)
    {
        n.e1.accept(this);
        n.e2.accept(this);
    }
    public void visit(Times n)
    {
        n.e1.accept(this);
        n.e2.accept(this);
    }
    public void visit(ArrayLookup n)
    {
        n.e1.accept(this);
        n.e2.accept(this);
    }
    public void visit(ArrayLength n)
    {
        n.e.accept(this);
    }
    public void visit(Call n)
    {
        // ignore identifier of method for now; Type Analysis
        // will handle those errors.

        n.e.accept(this);
        for (int i = 0; i < n.el.size(); ++i)
            n.el.elementAt(i).accept(this);
    }
    public void visit(IntegerLiteral n) { }
    public void visit(True n) { }
    public void visit(False n) { }
    public void visit(IdentifierExp n)
    {
        if (!_symbolTable.hasSymbol(n.s))
            recordUnknownSymbolError(n.s, n.getLine(), n.getColumn());
    }
    public void visit(This n)
    {

    }
    public void visit(NewArray n)
    {
        n.e.accept(this);
    }
    public void visit(NewObject n)
    {
        if (!_symbolTable.hasSymbol(n.i.s))
            recordUnknownSymbolError(n.i.s, n.getLine(), n.getColumn());
    }
    public void visit(Not n)
    {
        n.e.accept(this);
    }
    public void visit(Identifier n)
    {

    }
}
