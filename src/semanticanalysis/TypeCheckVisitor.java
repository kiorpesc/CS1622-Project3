package semanticanalysis;

import symboltable.*;
import syntaxtree.*;
import visitor.TypeVisitor;

import java.util.*;

public class TypeCheckVisitor extends ErrorChecker implements TypeVisitor 
{
    private ISymbolTable _symbolTable;

    public TypeCheckVisitor(ISymbolTable symbolTable)
    {
        _symbolTable = symbolTable;
    }

    private void checkInvalidRvalue(Exp rhs)
    {
        if (rhs instanceof IdentifierExp)
        {
            String name = ((IdentifierExp)rhs).s;
            SymbolInfo symbol = _symbolTable.getSymbol(name);
            if (symbol != null && !symbol.isRValue())                        
                addError("Invalid r-value: " + symbol.getName() + " is a " + symbol.getSymbolType(), rhs.getLine(), rhs.getColumn());
        }
    }

    private boolean areTypesCompatible(Type lhs, Type rhs)
    {
        if (lhs instanceof IdentifierType && rhs instanceof IdentifierType)
        {
            // navigate inheritance hierarchy, rhs must be subclass of lhs
            ClassSymbol lhsClass = _symbolTable.getClass(lhs.getName());
            ClassSymbol rhsClass = _symbolTable.getClass(rhs.getName());

            // continue searching until we find a match in the hierarchy
            while (rhsClass != null && !lhsClass.getName().equals(rhsClass.getName()))
            {
                rhsClass = getParent(rhsClass);
            }

            // rhsClass != null means we ended due to a match
            return rhsClass != null;
        }

        return lhs.equals(rhs);
    }

    private ClassSymbol getParent(ClassSymbol symbol)
    {
        String parentName = symbol.getParentName();
        return (parentName == null) ? null : _symbolTable.getClass(parentName);
    }

    private void validateIntegerOperator(Exp lhs, Exp rhs, String op)
    {
        if (!(lhs.accept(this) instanceof IntegerType) || !(rhs.accept(this) instanceof IntegerType))
            addError("Non-integer operand for operator " + op, lhs.getLine(), lhs.getColumn());

    }

    private List<Type> expressionsToTypes(ExpList list)
    {
        List<Type> result = new ArrayList<Type>();

        for (int i = 0; i < list.size(); ++i)
            result.add(list.elementAt(i).accept(this));

        return result;
    }

    public Type visit(Program n)
    {
        n.m.accept(this);

        for (int i = 0; i < n.cl.size(); ++i)
            n.cl.elementAt(i).accept(this);

        return null;
    }
    public Type visit(MainClass n)
    {
        if (!_symbolTable.hasClass(n.i1.s))
            return null;

        _symbolTable.enterClass(n.i1.s);
        try
        {
            _symbolTable.enterMethod("main");
            n.s.accept(this);
        }
        catch (NoSuchScopeException e)
        {
            _symbolTable.exitClass();
            return null;
        }

        _symbolTable.exitMethod();
        _symbolTable.exitClass();
    

        return null;
    }
    public Type visit(ClassDeclSimple n)
    {
        if (!_symbolTable.hasClass(n.i.s))
            return null;

        _symbolTable.enterClass(n.i.s);
   
        // visit VarDecls
        if (n.vl != null)
        {
            for (int i = 0; i < n.vl.size(); ++i)
                n.vl.elementAt(i).accept(this);
        }

        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        _symbolTable.exitClass();

        return null;
    }
    public Type visit(ClassDeclExtends n)
    {
        if (!_symbolTable.hasClass(n.i.s))
            return null;

        _symbolTable.enterClass(n.i.s);

        // visit VarDecls
        if (n.vl != null)
        {
            for (int i = 0; i < n.vl.size(); ++i)
                n.vl.elementAt(i).accept(this);
        }

        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        _symbolTable.exitClass();

        return null;
    }
    public Type visit(VarDecl n)
    {
        // no need to check Type here
        return null;
    }
    public Type visit(MethodDecl n)
    {
        try
        {
            _symbolTable.enterMethod(n.i.s);
        }
        catch (NoSuchScopeException e)
        {
            return null;
        }
        
        // check StatementList and Expression
        for (int i = 0; i < n.sl.size(); ++i)
            n.sl.elementAt(i).accept(this);

        n.e.accept(this);

        _symbolTable.exitMethod();
        return null;
    }
    public Type visit(Formal n)
    {
        return null;
    }
    public Type visit(IntArrayType n)
    {
        return null;
    }
    public Type visit(BooleanType n)
    {
        return null;
    }
    public Type visit(IntegerType n)
    {
        return null;
    }
    public Type visit(IdentifierType n)
    {
        return null;
    }
    public Type visit(Block n)
    {
        for (int i = 0; i < n.sl.size(); ++i)
            n.sl.elementAt(i).accept(this);

        return null;
    }
    public Type visit(If n)
    {
        Type conditionalType = n.e.accept(this);
        if (!(conditionalType instanceof BooleanType))
            addError("Non-boolean expression used as the condition of if statement", n.getLine(), n.getColumn());

        n.s1.accept(this);
        n.s2.accept(this);

        return null;
    }
    public Type visit(While n)
    {
        Type conditionalType = n.e.accept(this);
        if (!(conditionalType instanceof BooleanType))
            addError("Non-boolean expression used as the condition of while statement", n.getLine(), n.getColumn());

        n.s.accept(this);
        return null;
    }
    public Type visit(Print n)
    {
        Type expType = n.e.accept(this);
        if (!(expType instanceof IntegerType))
            addError("Call of method System.out.println does not match its declared signature", n.getLine(), n.getColumn());

        return null;
    }
    public Type visit(Assign n)
    {
        SymbolInfo symbol = _symbolTable.getSymbol(n.i.s);

        // Check for validity of lvalue
        if (symbol != null && !symbol.isLValue())
        {
            addError("Invalid l-value, " + symbol.getName() + " is a " + symbol.getSymbolType(), n.getLine(), n.getColumn());
        }

        Type expType = n.e.accept(this);

        // TODO: check for invalid r-value
        if (symbol instanceof VariableSymbol)
        {
            // Check if types match for assignment
            VariableSymbol variable = (VariableSymbol)symbol;
            Type variableType = variable.getType();

            if (!areTypesCompatible(variableType, expType))
                addError("Type mismatch during assignment", n.getLine(), n.getColumn());
        }
        checkInvalidRvalue(n.e);

        return null;
    }

    public Type visit(ArrayAssign n)
    {
        // We should check this, but the project prompt doesn't say anything aout it.
        Type indexType = n.e1.accept(this);        

        Type rhsType = n.e2.accept(this);

        // Arrays are only integers
        if (!(rhsType instanceof IntegerType))
            addError("Type mismatch during assignment", n.getLine(), n.getColumn());

        checkInvalidRvalue(n.e2);

        return null;
    }
    public Type visit(And n)
    {
        Type lhsType = n.e1.accept(this);
        Type rhsType = n.e2.accept(this);

        if (!(lhsType instanceof BooleanType) || !(rhsType instanceof BooleanType))
            addError("Attempt to use boolean operator && on non-boolean operands", n.getLine(), n.getColumn());

        return new BooleanType();
    }
    public Type visit(LessThan n)
    {
        validateIntegerOperator(n.e1, n.e2, "<");
        return new BooleanType();        
    }
    public Type visit(Plus n)
    {
        validateIntegerOperator(n.e1, n.e2, "+");
        return new IntegerType();
    }
    public Type visit(Minus n)
    {
        validateIntegerOperator(n.e1, n.e2, "-");
        return new IntegerType();
    }
    public Type visit(Times n)
    {
        validateIntegerOperator(n.e1, n.e2, "*");
        return new IntegerType();
    }
    public Type visit(ArrayLookup n)
    {
        // TODO: should we check array type?
        Type arrayType = n.e1.accept(this);
        // TODO: should we check index type?
        Type indexType = n.e2.accept(this);

        // arrays are always int[]
        return new IntegerType();
    }
    public Type visit(ArrayLength n)
    {
        Type expType = n.e.accept(this);
        if (!(expType instanceof IntArrayType))
            addError("Length property only applies to arrays", n.getLine(), n.getColumn());

        return new IntegerType();
    }
    public Type visit(Call n)
    {
        Type expType = n.e.accept(this);
        // Evaluating type of expression may have failed...
        if (expType != null)
        {            
            ClassSymbol classType = _symbolTable.getClass(expType.getName());
            // may have tried to call method on non-object...
            if (classType != null)
            {
                // Method may not exist on the type...
                MethodSymbol method = classType.getMethod(n.i.s);
                if (method == null)
                {
                    addError("Attempt to call a non-method", n.getLine(), n.getColumn());
                }
                else
                {
                    List<Type> expTypes = expressionsToTypes(n.el);
                    List<Type> formalTypes = method.getFormalTypes();

                    // Validate number of arguments
                    if (expTypes.size() != formalTypes.size())
                    {
                        addError("Call of method " + method.getName() 
                                + " does not match its declared number of arguments", 
                                n.getLine(), n.getColumn());
                    }                    
                    else
                    {
                        // Validate type of arguments
                        for (int i = 0; i < expTypes.size(); ++i)
                        {
                            if (!areTypesCompatible(expTypes.get(i), formalTypes.get(i)))
                            {
                                addError("Call of method " + method.getName() + " does not match its declared signature",
                                        n.getLine(), n.getColumn());
                                break;
                            }
                        }                        
                    }

                    // return the return type of the method
                    return method.getReturnType();
                }
                // TODO: if method doesn't exist, should we return a type? 
                // right now, just default to null                
            }
        }
        return null;
    }
    public Type visit(IntegerLiteral n)
    {
        return new IntegerType();
    }
    public Type visit(True n)
    {
        return new BooleanType();
    }
    public Type visit(False n)
    {
        return new BooleanType();
    }
    public Type visit(IdentifierExp n)
    {
        SymbolInfo symbol = _symbolTable.getSymbol(n.s);
        if (symbol instanceof VariableSymbol)
        {
            VariableSymbol varSymbol = (VariableSymbol)symbol;
            return varSymbol.getType();
        }
        return null;
    }
    public Type visit(This n)
    {
        if (_symbolTable.getCurrentMethod().getName().equals("main"))
            addError("Illegal use of the keyword 'this' in static method", n.getLine(), n.getColumn());

        return new IdentifierType(_symbolTable.getCurrentClass().getName());
    }
    public Type visit(NewArray n)
    {
        return new IntArrayType();
    }
    public Type visit(NewObject n)
    {
        return new IdentifierType(n.i.s);
    }
    public Type visit(Not n)
    {
        Type expType = n.e.accept(this);
        if (!(expType instanceof BooleanType))
            addError("Attempt to use boolean operator ! on non-boolean operand", n.getLine(), n.getColumn());

        return new BooleanType();
    }
    public Type visit(Identifier n)
    {
        return null;
    }
}