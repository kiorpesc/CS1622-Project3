package optimization;

import java.util.*;

import controlflow.*;
import irgeneration.*;
import symboltable.*;

public class ConstantPropagator
{
    private boolean _optimized = false;

    public ConstantPropagator(List<IRQuadruple> irList, List<ControlFlowGraph> cfgs)
    {
        List<Integer> copyIndices = new ArrayList<Integer>();

        for (int i = 0; i < irList.size(); ++i)
        {
            IRQuadruple irq = irList.get(i);
            if (isConstantCopy(irq))
            {
                copyIndices.add(i);
            }
        }

        for (Integer statementIndex : copyIndices)
        {
            IRQuadruple irq = irList.get(statementIndex);
            ControlFlowGraph cfg = getCFGFromStatement(cfgs, irq);
            Set<IRQuadruple> reducibles = getReducibleStatements(cfg, irq);

            _optimized = propagateConstant(irList, irq, reducibles);
        }
    }

    public boolean wasOptimized()
    {
        return _optimized;
    }

    // propagates the constant to all qualifying statements.
    // returns true if at least one statement was modified.
    private boolean propagateConstant(List<IRQuadruple> irList, IRQuadruple constCopy, Set<IRQuadruple> reducibles)
    {
        // first, remove the copy from the IR
        //irList.remove(constCopy);
        SymbolInfo removed = constCopy.getResult();
        SymbolInfo constant = constCopy.getArg1();

        boolean result = false;

        for (int i = 0; i < irList.size(); ++i)
        {
            IRQuadruple next = irList.get(i);

            if (!reducibles.contains(next))
                continue;

            // need to handle ArrayAssign specially, since the result is
            // actually the rhs of the statement.
            if (next instanceof IRArrayAssign && next.getResult() == removed)
            {
                next = new IRArrayAssign(next.getArg1(), next.getArg2(), constant);
                result = true;
            }

            if (next.replaceArgs(removed, constant))
                result = true;
        }

        return result;
    }

    private boolean isConstantCopy(IRQuadruple irq)
    {
        return (irq instanceof IRCopy) && (irq.getArg1() instanceof ConstantSymbol);
    }

    // gets the CFG that contains the IR statement
    private ControlFlowGraph getCFGFromStatement(List<ControlFlowGraph> cfgs, IRQuadruple irq)
    {
        for (ControlFlowGraph g : cfgs)
        {
            for (BasicBlock b : g.getAllBlocks())
            {
                if (b.contains(irq))
                    return g;
            }
        }

        throw new IllegalArgumentException("statement not found in any CFG");
    }

    // gets the BasicBlock that contains the IRStatement
    private BasicBlock getBasicBlockFromStatement(ControlFlowGraph graph, IRQuadruple irq)
    {
        for (BasicBlock b : graph.getAllBlocks())
        {
            if (b.contains(irq))
                return b;
        }
        throw new IllegalArgumentException("statement not found in any BasicBlock");
    }

    // collect all of the IRQuadruple statements that the constant could be propagated to
    private Set<IRQuadruple> getReducibleStatements(ControlFlowGraph graph, IRQuadruple origDef)
    {
        BasicBlock start = getBasicBlockFromStatement(graph, origDef);

        Set<IRQuadruple> reducibles = new HashSet<IRQuadruple>();

        // get reducible statements from the current block
        for (int i = start.indexOf(origDef) + 1; i < start.size(); ++i)
        {
            IRQuadruple next = start.getStatement(i);

            if (isConflictingDef(next, origDef))
                return reducibles;
            reducibles.add(next);
        }

        // recursively gather reducible statements from successor blocks
        Set<BasicBlock> visited = new HashSet<BasicBlock>();
        visited.add(start);
        for (BasicBlock succ : graph.getSuccessors(start))
        {
            getReducibleStatements(graph, succ, origDef, visited, reducibles);
        }

        return reducibles;
    }

    private void getReducibleStatements(ControlFlowGraph graph, BasicBlock current,
                                        IRQuadruple origDef, Set<BasicBlock> visited, Set<IRQuadruple> reducibles)
    {
        // visit the current block
        visited.add(current);

        // bust out if a predecessor contains a conflicting definition
        // we can pass visited here because all of our visited nodes are valid,
        // otherwise we would have escaped
        if (parentsContainConflictingDef(graph, current, origDef, new HashSet<BasicBlock>(visited)))
        {
            visited.remove(current);
            return;
        }

        // loop over the statements of the current block
        for (IRQuadruple irq : current.getStatements())
        {
            // check if we found a conflicting definition of the candidate along this path
            if (isConflictingDef(irq, origDef))
            {
                visited.remove(current);
                return;
            }
            reducibles.add(irq);
        }

        // check successors for reducible statements
        for (BasicBlock succ : graph.getSuccessors(current))
        {
            if (!visited.contains(succ))
            {
                getReducibleStatements(graph, succ, origDef, visited, reducibles);
            }
        }

        visited.remove(current);
    }

    // returns true iff some block along the path of predecessors from current contains a def
    // that conflicts with origDef
    private boolean parentsContainConflictingDef(ControlFlowGraph graph, BasicBlock current,
                                                    IRQuadruple origDef, Set<BasicBlock> visited)
    {
        visited.add(current);

        //System.out.println("visited parents: " + visited);

        for (BasicBlock pred : graph.getPredecessors(current))
        {
            if (!visited.contains(pred))
            {
                if (containsConflictingDef(pred, origDef) || parentsContainConflictingDef(graph, pred, origDef, visited))
                {
                    visited.remove(current);
                    return true;
                }
            }
        }

        visited.remove(current);
        return false;
    }

    // determine if a statement is a definition of the candidate
    private boolean isDefOf(IRQuadruple irq, SymbolInfo candidate)
    {
        return !(irq instanceof IRArrayAssign) && irq.getResult() == candidate;
    }

    private boolean isConflictingDef(IRQuadruple def, IRQuadruple origDef)
    {
        return (def != origDef && isDefOf(def, origDef.getResult()));
    }

    // returns true iff b contains a definition that conflicts with origDef
    // i.e., if origDef is t := x, then b contains
    private boolean containsConflictingDef(BasicBlock b, IRQuadruple origDef)
    {
        for (IRQuadruple irq : b.getStatements())
        {
            if (isConflictingDef(irq, origDef))
                return true;
        }

        return false;
    }
}