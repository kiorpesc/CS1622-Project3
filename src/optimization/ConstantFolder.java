package optimization;

import java.util.*;
import irgeneration.*;
import symboltable.*;
import syntaxtree.*;

public class ConstantFolder
{
    private boolean _wasOptimized = false;

    // folds constant expressions into single constant values by
    // evaluation them at compile-time.
    public ConstantFolder(List<IRQuadruple> irList)
    {
        for (int i = 0; i < irList.size(); ++i)
        {
            IRQuadruple irq = irList.get(i);
            if (irq instanceof IRAssignment)
            {
                irList.set(i, tryFoldAssignment((IRAssignment)irq, irList));
            }
        }
    }
    public boolean wasOptimized()
    {
        return _wasOptimized;
    }
    private IRQuadruple tryFoldAssignment(IRAssignment n, List<IRQuadruple> irList)
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
            return new IRCopy(new ConstantSymbol(constant, constantType), n.getResult());
        }
        return n;
    }
}