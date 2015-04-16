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

        if (!_currentBlock.isEmpty())
        {
            _currentCfg.addBlock(_currentBlock);
            finalizeCfg(_currentCfg);
            _controlFlowGraphs.add(_currentCfg);
        }

        _currentCfg = null;
        _currentBlock = null;
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

    private BasicBlock endBlock()
    {
        _currentCfg.addBlock(_currentBlock);
        BasicBlock previous = _currentBlock;
        _currentBlock = new BasicBlock();
        return previous;
    }

    private void addEdgeFrom(BasicBlock previous)
    {
        _currentCfg.addEdge(previous, _currentBlock);
    }

    private void endBlockAndAddEdge()
    {
        BasicBlock previous = endBlock();
        addEdgeFrom(previous);
    }

    public void visit(IRArrayAssign n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRArrayLength n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRArrayLookup n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRAssignment n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRCall n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRCondJump n)
    {
        // just add the statement, we'll add an edge for the jump
        // later.
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRCopy n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRLabel n)
    {
        // check if our current block is non-empty
        if (!_currentBlock.isEmpty())
        {
            // save the current block, since a label is always the target
            // of a branch or jump
            BasicBlock previous = endBlock();

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
                addEdgeFrom(previous);
            }

        }
        _currentBlock.addStatement(n);
        _labelsToBlocks.put(n.getLabel(), _currentBlock);
    }
    public void visit(IRNewArray n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRNewObject n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
    }
    public void visit(IRParam n)
    {
        _currentBlock.addStatement(n);
        endBlockAndAddEdge();
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
        endBlock();
    }
    public void visit(IRUncondJump n)
    {
        // just add the statement, we'll add an edge for the jump
        // later.
        _currentBlock.addStatement(n);
        endBlock();

        // no need to add a successor edge for fallthrough,
        // since the jump is unconditional it will always be taken.
    }
}