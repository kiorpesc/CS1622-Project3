package controlflow;

import symboltable.*;
import irgeneration.*;

import java.util.*;


// Performs liveness analysis on a CFG
// Determines live-in and live-out variables for each block
public class LivenessAnalysis
{
    private Map<BasicBlock, Set<SymbolInfo>> _liveIn;
    private Map<BasicBlock, Set<SymbolInfo>> _liveOut;
    private CFGDefsAndUses _defsAndUses;

    public LivenessAnalysis(ControlFlowGraph graph)
    {
        _liveIn = new HashMap<BasicBlock, Set<SymbolInfo>>();
        _liveOut = new HashMap<BasicBlock, Set<SymbolInfo>>();
        _defsAndUses = new CFGDefsAndUses(graph);

        calculateLiveness(graph);
    }

    public Set<SymbolInfo> getLiveIn(BasicBlock b)
    {
        return _liveIn.get(b);
    }

    public Set<SymbolInfo> getLiveOut(BasicBlock b)
    {
        return _liveOut.get(b);
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();
        for (BasicBlock b : _liveIn.keySet())
        {
            result.append("BLOCK\n");
            result.append(b.toString());
            result.append("IN:\n");
            for (SymbolInfo in : getLiveIn(b))
            {
                result.append('\t');
                result.append(in.toString());
            }
            result.append("OUT:\n");
            for (SymbolInfo out : getLiveOut(b))
            {
                result.append('\t');
                result.append(out.toString());
            }
            result.append("END BLOCK\n");
        }
        return result.toString();
    }

    private void calculateLiveness(ControlFlowGraph graph)
    {
        // initialize to empty sets
        for (BasicBlock b : graph.getAllBlocks())
        {
            _liveIn.put(b, new HashSet<SymbolInfo>());
            _liveOut.put(b, new HashSet<SymbolInfo>());
        }

        boolean changed = false;
        do
        {
            changed = false;
            for (BasicBlock b : graph.getAllBlocks())
            {
                // get new in set
                Set<SymbolInfo> newIn = computeLiveIn(b);
                // get new out set
                Set<SymbolInfo> newOut = computeLiveOut(graph, b);

                // compare new live-in/out with the old sets
                Set<SymbolInfo> oldIn = _liveIn.get(b);
                Set<SymbolInfo> oldOut = _liveOut.get(b);

                if (!newIn.equals(oldIn) || !newOut.equals(oldOut))
                {
                    _liveIn.put(b, newIn);
                    _liveOut.put(b, newOut);
                    changed = true;
                }
            }
        } while (changed);
    }

    private Set<SymbolInfo> computeLiveIn(BasicBlock b)
    {
        // initialize with use[b]
        Set<SymbolInfo> in = new HashSet<SymbolInfo>();
        in.addAll(_defsAndUses.getUsages(b));

        // compute out[n] - def[n]
        Set<SymbolInfo> outsLessDefs = new HashSet<SymbolInfo>();
        outsLessDefs.addAll(_liveOut.get(b));
        outsLessDefs.removeAll(_defsAndUses.getDefinitions(b));

        // in = use[b] U (out[n] - def[n])
        in.addAll(outsLessDefs);
        return in;
    }

    private Set<SymbolInfo> computeLiveOut(ControlFlowGraph graph, BasicBlock b)
    {
        Set<SymbolInfo> out = new HashSet<SymbolInfo>();

        // out is union of all of the successor's ins
        for (BasicBlock succ : graph.getSuccessors(b))
        {
            out.addAll(_liveIn.get(succ));
        }

        return out;
    }
}
