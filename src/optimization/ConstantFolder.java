package optimization;

import java.util.*;
import irgeneration.*;
import symboltable.*;
import syntaxtree.*;

public class ConstantFolder implements IRVisitor
{
    private List<IRQuadruple> _optimizedIr;
    private int _statementIndex = 0;
    private boolean _wasOptimized = false;

    // folds constant expressions into single constant values by
    // evaluation them at compile-time.
    public ConstantFolder(List<IRQuadruple> irList)
    {
        _optimizedIr = irList;
        for (IRQuadruple irq : _optimizedIr)
        {
            irq.accept(this);
            _statementIndex++;
        }
    }
    public boolean wasOptimized()
    {
        return _wasOptimized;
    }
    public void visit(IRArrayAssign n) { }
    public void visit(IRArrayLength n) { }
    public void visit(IRArrayLookup n) { }
    public void visit(IRAssignment n)
    {
        SymbolInfo arg1 = n.getArg1();
        SymbolInfo arg2 = n.getArg2();

        // EWWWWW
        if (arg1 instanceof ConstantSymbol && arg2 instanceof ConstantSymbol)
        {
            String value1 = ((ConstantSymbol)arg1).getValue();
            String value2 = ((ConstantSymbol)arg2).getValue();

            String constant = "";
            Type constantType = null;
            switch (n.getOp())
            {
                case "+":
                    constant = "" + (Integer.parseInt(value1) + Integer.parseInt(value2));
                    constantType = new IntegerType();
                    break;
                case "-":
                    constant = "" + (Integer.parseInt(value1) - Integer.parseInt(value2));
                    constantType = new IntegerType();
                    break;
                case "*":
                    constant = "" + (Integer.parseInt(value1) * Integer.parseInt(value2));
                    constantType = new IntegerType();
                    break;
                case "&&":
                    constant = "" + (Boolean.parseBoolean(value1) && Boolean.parseBoolean(value2));
                    constantType = new BooleanType();
                    break;
                case "<":
                    constant = "" + (Integer.parseInt(value1) < Integer.parseInt(value2));
                    constantType = new BooleanType();
                    break;

                default:
                    throw new IllegalArgumentException("unexpected operator in assignment");
            }

            _wasOptimized = true;
            IRCopy replacement = new IRCopy(new ConstantSymbol(constant, constantType), n.getResult());
            _optimizedIr.set(_statementIndex, replacement);
        }
    }
    public void visit(IRCall n) { }
    public void visit(IRCondJump n) { }
    public void visit(IRCopy n) { }
    public void visit(IRLabel n) { }
    public void visit(IRNewArray n) { }
    public void visit(IRNewObject n) { }
    public void visit(IRParam n) { }
    public void visit(IRReturn n) { }
    public void visit(IRUnaryAssignment n) { }
    public void visit(IRUncondJump n) { }
}