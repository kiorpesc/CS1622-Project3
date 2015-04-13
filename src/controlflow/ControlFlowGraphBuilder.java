package controlflow;

import java.util.*;
import irgeneration.*;

// Visits IRList and constructs a list of ControlFlowGraphs (one for each method)
public class ControlFlowGraphBuilder implements IRVisitor
{
    private ControlFlowGraph _currentCfg = new ControlFlowGraph();
    private BasicBlock _currentBlock = new BasicBlock();

    private Map<String, BasicBlock> _labelsToBlocks = new HashMap<String, BasicBlock>();
    private List<ControlFlowGraph> _controlFlowGraphs = new ArrayList<ControlFlowGraph>();

    public ControlFlowGraphBuilder(List<IRQuadruple> irList)
    {
        for (IRQuadruple irq : irList)
            irq.accept(this);
    }

    public List<ControlFlowGraph> getControlFlowGraphs()
    {
        return _controlFlowGraphs;
    }

    private void finalizeCfg(ControlFlowGraph g)
    {
        // for each basic block
        for (BasicBlock b : g.getAllBlocks())
        {
            // look at the block's statements
            for (IRQuadruple statement : b.getStatements())
            {
                // TODO: better way of doing this than instanceof?
                if (statement instanceof IRUncondJump)
                {
                    // add an edge to the jumped-to block
                    IRUncondJump jump = (IRUncondJump)statement;
                    BasicBlock successor = _labelsToBlocks.get(jump.getLabel());
                    g.addEdge(b, successor);
                }

                else if (statement instanceof IRCondJump)
                {
                    // add an edge to the jumped-to block
                    IRCondJump jump = (IRCondJump)statement;
                    BasicBlock successor = _labelsToBlocks.get(jump.getLabel());
                    g.addEdge(b, successor);
                }
            }
        }
        // clear out the mapping between labels and blocks
        _labelsToBlocks.clear();
    }
    public void visit(IRArrayAssign n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRArrayLength n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRArrayLookup n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRAssignment n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRCall n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRCondJump n)
    {
        // just add the statement, we'll add an edge for the jump
        // later.
        _currentBlock.addStatement(n);
        _currentCfg.addBlock(_currentBlock);

        // end the current block
        BasicBlock previous = _currentBlock;
        _currentBlock = new BasicBlock();

        // add an edge from the previous block to the current one
        // to account for fallthrough.
        _currentCfg.addEdge(previous, _currentBlock);
    }
    public void visit(IRCopy n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRLabel n)
    {
        // check if our current block is non-empty
        if (!_currentBlock.isEmpty())
        {
            // save the current block, since a label is always the target
            // of a branch or jump
            _currentCfg.addBlock(_currentBlock);
            BasicBlock previous = _currentBlock;
            _currentBlock = new BasicBlock();

            if (n.isMethod())
            {
                // if we're starting a new method,
                // end the current control flow graph.
                _controlFlowGraphs.add(_currentCfg);
                finalizeCfg(_currentCfg);
                _currentCfg = new ControlFlowGraph();
            }
            else
            {
                // non-method label
                // add edge from previous block to the next block
                _currentCfg.addEdge(previous, _currentBlock);
            }

        }
        _currentBlock.addStatement(n);
        _labelsToBlocks.put(n.getLabel(), _currentBlock);
    }
    public void visit(IRNewArray n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRNewObject n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRParam n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRReturn n)
    {
        _currentBlock.addStatement(n);
        _currentCfg.addBlock(_currentBlock);
        _controlFlowGraphs.add(_currentCfg);

        finalizeCfg(_currentCfg);

        _currentBlock = new BasicBlock();
        _currentCfg = new ControlFlowGraph();
    }
    public void visit(IRUnaryAssignment n)
    {
        _currentBlock.addStatement(n);
    }
    public void visit(IRUncondJump n)
    {
        // just add the statement, we'll add an edge for the jump
        // later.
        _currentBlock.addStatement(n);
        _currentCfg.addBlock(_currentBlock);

        // end the current block
        BasicBlock previous = _currentBlock;
        _currentBlock = new BasicBlock();

        // no need to add a successor edge for fallthrough,
        // since the jump is unconditional it will always be taken.
    }
}