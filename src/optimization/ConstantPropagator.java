package optimization;

import java.util.*;

import controlflow.*;
import irgeneration.*;
import objectimpl.*;
import symboltable.*;

public class ConstantPropagator
{
    private boolean _optimized = false;

    public ConstantPropagator(List<IRQuadruple> irList, List<ControlFlowGraph> cfgs, ObjectLayoutManager objLayoutMgr)
    {
        List<Integer> copyIndices = new ArrayList<Integer>();

        for (int i = 0; i < irList.size(); ++i)
        {
            IRQuadruple irq = irList.get(i);
            // don't even try to do constant propagation for instance variables, since
            // method calls in between uses/defs can change their value
            if (isConstantCopy(irq) && !objLayoutMgr.isInstanceVariable(irq.getResult()))
            {
                copyIndices.add(i);
            }
        }

        for (Integer statementIndex : copyIndices)
        {
            IRQuadruple irq = irList.get(statementIndex);
            ControlFlowGraph cfg = CFGUtility.getCFGFromStatement(cfgs, irq);
            Set<IRQuadruple> reducibles = getReducibleStatements(cfg, irq);

            if (propagateConstant(irList, irq, reducibles))
                _optimized = true;
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
        SymbolInfo removed = constCopy.getResult();
        SymbolInfo constant = constCopy.getArg1();

        boolean result = false;

        for (int i = 0; i < irList.size(); ++i)
        {
            IRQuadruple next = irList.get(i);

            if (!reducibles.contains(next))
                continue;

            if (next.replaceArgs(removed, constant))
                result = true;
        }

        return result;
    }

    private boolean isConstantCopy(IRQuadruple irq)
    {
        return (irq instanceof IRCopy) && (irq.getArg1() instanceof ConstantSymbol);
    }

    // collect all of the IRQuadruple statements that the constant could be propagated to
    private Set<IRQuadruple> getReducibleStatements(ControlFlowGraph graph, IRQuadruple origDef)
    {
        BasicBlock start = CFGUtility.getBasicBlockFromStatement(graph, origDef);

        Set<IRQuadruple> reducibles = new HashSet<IRQuadruple>();

        // get reducible statements from the current block
        // start at the statement immediately after ours
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

    private boolean isConflictingDef(IRQuadruple def, IRQuadruple origDef)
    {
        return (def != origDef && def.isDefOf(origDef.getResult()));
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