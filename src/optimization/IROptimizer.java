package optimization;

import java.util.*;

import irgeneration.*;
import controlflow.*;
import objectimpl.*;

public class IROptimizer
{
    private List<IRQuadruple> _irList;
    private ObjectLayoutManager _objLayoutMgr;

    public IROptimizer(List<IRQuadruple> irList, ObjectLayoutManager objLayoutMgr)
    {
        // copy the IR so we can safely optimize it
        _irList = new ArrayList<IRQuadruple>(irList);
        _objLayoutMgr = objLayoutMgr;
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
        // we might be able to more smartly repeat the optimizations,
        // but whatever yolo
        // build our CFGS for the IR
        ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder(_irList);
        Collection<ControlFlowGraph> cfgs = cfgBuilder.getControlFlowGraphs();

        ConstantFolder folder = new ConstantFolder(_irList);
        // rebuild CFG in case folder was optimized, since folding
        // changes the IR
        if (folder.wasOptimized())
        {
            cfgBuilder = new ControlFlowGraphBuilder(_irList);
            cfgs = cfgBuilder.getControlFlowGraphs();
        }
        ConstantPropagator prop = new ConstantPropagator(_irList, cfgs);
        // don't need to rebuild CFG, since propagating just changes
        // arguments
        DeadCodeEliminator elim = new DeadCodeEliminator(_irList, cfgs, _objLayoutMgr);

        return folder.wasOptimized() || prop.wasOptimized() || elim.wasOptimized();
    }
}
