package optimization;

import java.util.*;

import irgeneration.*;
import controlflow.*;

public class CFGUtility
{
    // gets the CFG that contains the IR statement
    public static ControlFlowGraph getCFGFromStatement(List<ControlFlowGraph> cfgs, IRQuadruple irq)
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
    public static BasicBlock getBasicBlockFromStatement(ControlFlowGraph graph, IRQuadruple irq)
    {
        for (BasicBlock b : graph.getAllBlocks())
        {
            if (b.contains(irq))
                return b;
        }
        throw new IllegalArgumentException("statement not found in any BasicBlock");
    }

}