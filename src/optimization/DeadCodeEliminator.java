package optimization;

import java.util.*;

import controlflow.*;
import irgeneration.*;
import symboltable.*;

public class DeadCodeEliminator
{

    private boolean _optimized = false;

    public DeadCodeEliminator(List<ControlFlowGraph> graphs, List<IRQuadruple> irList)
    {
        Set<IRQuadruple> removable = new HashSet<IRQuadruple>();

        for (IRQuadruple irq : irList)
        {
            if (isConstantCopy(irq))
            {
                ControlFlowGraph graph = CFGUtility.getCFGFromStatement(graphs, irq);
                BasicBlock b = CFGUtility.getBasicBlockFromStatement(graph, irq);

                //System.out.println("attempting to reduce: " + irq);
                if (!containsUsageBeforeDef(graph, b, irq))
                {
                    removable.add(irq);
                }
            }
        }

        if (!removable.isEmpty())
            _optimized = true;

        for (IRQuadruple r : removable)
            irList.remove(r);
    }

    public boolean wasOptimized()
    {
        return _optimized;
    }

    private boolean containsUsageBeforeDef(ControlFlowGraph graph, BasicBlock containsDef, IRQuadruple candidate)
    {
        //System.out.println("CFG: " + graph);

        SymbolInfo sym = candidate.getResult();

        for (int i = containsDef.indexOf(candidate) + 1; i < containsDef.size(); ++i)
        {
            IRQuadruple next = containsDef.getStatement(i);

            // NOTE: we check usage before def, since the next statement
            // could be both a def and usage. in this case, we can't eliminate
            // the original def.

            // if the containing block contains a usage, forgetaboutit
            if (next.isUsageOf(sym))
                return true;

            // if we define the symbol in the same block immediately,
            // we can just return now
            if (next.isDefOf(sym))
                return false;
        }

        return checkSuccsForUsagesBeforeDef(graph, containsDef, sym, new HashSet<BasicBlock>());
    }

    private boolean checkSuccsForUsagesBeforeDef(ControlFlowGraph graph, BasicBlock b, SymbolInfo sym, Set<BasicBlock> visited)
    {
        visited.add(b);

        for (BasicBlock succ : graph.getSuccessors(b))
        {
            if (visited.contains(succ))
                continue;

            // if any of the successors paths encounter a usage before a def,
            // return true
            if (containsUsageBeforeDef(graph, succ, sym, visited))
            {
                visited.remove(b);
                return true;
            }
        }

        visited.remove(b);

        return false;
    }

    private boolean containsUsageBeforeDef(ControlFlowGraph graph, BasicBlock b, SymbolInfo sym, Set<BasicBlock> visited)
    {
        //System.out.println("visiting: " + b);
        for (IRQuadruple irq : b.getStatements())
        {
            if (irq.isUsageOf(sym))
                return true;

            if (irq.isDefOf(sym))
                return false;
        }

        return checkSuccsForUsagesBeforeDef(graph, b, sym, visited);
    }

    private boolean isConstantCopy(IRQuadruple irq)
    {
        return (irq instanceof IRCopy) && (irq.getArg1() instanceof ConstantSymbol);
    }
}
