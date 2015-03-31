import symboltable.*;
import syntaxtree.*;
import visitor.TypeVisitor;

import java.util.*;

public class TypeCheckVisitor implements TypeVisitor 
{
    private ISymbolTable _symbolTable;
    private List<String> _errors = new ArrayList<String>();

    public TypeCheckVisitor(ISymbolTable symbolTable)
    {
        _symbolTable = symbolTable;
    }

    public List<String> getErrors()
    {
        return _errors;
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
            _errors.add("Non-integer operand for operator " + op);

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
        _symbolTable.enterClass(n.i1.s);
        _symbolTable.enterMethod("main");

        n.s.accept(this);

        _symbolTable.exitMethod();
        _symbolTable.exitClass();

        return null;
    }
    public Type visit(ClassDeclSimple n)
    {
        _symbolTable.enterClass(n.i.s);

        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);

        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        _symbolTable.exitClass();

        return null;
    }
    public Type visit(ClassDeclExtends n)
    {
        _symbolTable.enterClass(n.i.s);

        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);

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
        _symbolTable.enterMethod(n.i.s);

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
            _errors.add("Non-boolean expression used as the condition of if statement");

        n.s1.accept(this);
        n.s2.accept(this);
        return null;
    }
    public Type visit(While n)
    {
        Type conditionalType = n.e.accept(this);
        if (!(conditionalType instanceof BooleanType))
            _errors.add("Non-boolean expression used as the condition of while statement");

        n.s.accept(this);
        return null;
    }
    public Type visit(Print n)
    {
        Type expType = n.e.accept(this);
        if (!(expType instanceof IntegerType))
            _errors.add("Call of method System.out.println does not match its declared signature");

        return null;
    }
    public Type visit(Assign n)
    {
        SymbolInfo symbol = _symbolTable.getSymbol(n.i.s);

        // Check for validity of lvalue
        if (!symbol.isLValue())
        {
            _errors.add("Invalid l-value, " + symbol.getName() + " is a " + symbol.getSymbolType());
        }

        Type expType = n.e.accept(this);

        // TODO: check for invalid r-value
        if (symbol instanceof VariableSymbol)
        {
            // Check if types match for assignment
            VariableSymbol variable = (VariableSymbol)symbol;
            Type variableType = variable.getType();

            if (!areTypesCompatible(variableType, expType))
                _errors.add("Type mismatch during assignment");
        }
        return null;
    }
    public Type visit(ArrayAssign n)
    {
        // We should check this, but the project prompt doesn't say anything aout it.
        Type indexType = n.e1.accept(this);        

        Type rhsType = n.e2.accept(this);

        // Arrays are only integers
        if (!(rhsType instanceof IntegerType))
            _errors.add("Type mismatch during assignment");

        return null;
    }
    public Type visit(And n)
    {
        Type lhsType = n.e1.accept(this);
        Type rhsType = n.e2.accept(this);

        if (!(lhsType instanceof BooleanType) || !(rhsType instanceof BooleanType))
            _errors.add("Attempt to use boolean operator && on non-boolean operands");

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
            _errors.add("Length property only applies to arrays");

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
                    _errors.add("Attempt to call a non-method");
                }
                else
                {
                    List<Type> expTypes = expressionsToTypes(n.el);
                    List<Type> formalTypes = method.getFormalTypes();

                    // Validate number of arguments
                    if (expTypes.size() != formalTypes.size())
                    {
                        _errors.add("Call of method " + method.getName() + " does not match its declared number of arguments");
                    }                    
                    else
                    {
                        // Validate type of arguments
                        for (int i = 0; i < expTypes.size(); ++i)
                        {
                            if (!areTypesCompatible(expTypes.get(i), formalTypes.get(i)))
                            {
                                _errors.add("Call of method " + method.getName() + " does not match its declared signature");
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
            _errors.add("Illegal use of the keyword 'this' in static method");

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
            _errors.add("Attempt to use boolean operator ! on non-boolean operand");

        return new BooleanType();
    }
    public Type visit(Identifier n)
    {
        return null;
    }
}