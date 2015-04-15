package optimization;

import java.util.*;

import irgeneration.*;
import controlflow.*;

public class IROptimizer
{
    private List<IRQuadruple> _irList;

    public IROptimizer(List<IRQuadruple> irList)
    {
        // copy the IR so we can safely optimize it
        _irList = new ArrayList<IRQuadruple>(irList);
    }

    public List<IRQuadruple> getOptimizedIR()
    {
        return _irList;
    }

    public void optimize()
    {
        // LOL
        while (optimizeOnce());
    }

    public boolean optimizeOnce()
    {
        // build our CFGS for the IR
        ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder(_irList);
        List<ControlFlowGraph> cfgs = cfgBuilder.getControlFlowGraphs();

        ConstantFolder folder = new ConstantFolder(_irList);
        ConstantPropagator prop = new ConstantPropagator(_irList, cfgs);
        DeadCodeEliminator elim = new DeadCodeEliminator(_irList, cfgs);

        return folder.wasOptimized() || prop.wasOptimized() || elim.wasOptimized();
    }
}