package controlflow;

import java.util.*;
import irgeneration.*;
import symboltable.*;

// Visits IRList and constructs a list of ControlFlowGraphs (one for each method)
public class ControlFlowGraphBuilder implements IRVisitor
{
    private ControlFlowGraph _currentCfg = new ControlFlowGraph();
    private BasicBlock _previous = null;
    private BasicBlock _currentBlock = new BasicBlock();
    private MethodSymbol _currentMethod = null;

    private Map<String, BasicBlock> _labelsToBlocks = new HashMap<String, BasicBlock>();
    private Map<MethodSymbol, ControlFlowGraph> _controlFlowGraphs = new HashMap<MethodSymbol, ControlFlowGraph>();

    public ControlFlowGraphBuilder(List<IRQuadruple> irList)
    {
        for (IRQuadruple irq : irList)
            irq.accept(this);

        // end the last block
        if (!_currentBlock.isEmpty())
        {
            // this should never happen, since every statement besides
            // a label ends a block, and nothing should end with a label.
            throw new IllegalStateException("didn't finish current block");
        }

        // finish the last CFG
        if (!_currentCfg.isEmpty())
        {
            addControlFlowGraph();
        }

        _currentCfg = null;
        _currentBlock = null;
        _currentMethod = null;
    }

    public Collection<ControlFlowGraph> getControlFlowGraphs()
    {
        return _controlFlowGraphs.values();
    }

    public Map<MethodSymbol, ControlFlowGraph> getMethodToCFGMap()
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
    }

    private void addControlFlowGraph()
    {
        finalizeCfg(_currentCfg);
         // clear out the mapping between labels and blocks
        _labelsToBlocks.clear();
        // set previous to null
        _previous = null;
        if (_currentMethod == null)
            throw new IllegalStateException("cannot associate CFG with no method");
        _controlFlowGraphs.put(_currentMethod, _currentCfg);
        _currentCfg = new ControlFlowGraph();
    }

    private void endBlock()
    {
        _currentCfg.addBlock(_currentBlock);
        _previous = _currentBlock;
        _currentBlock = new BasicBlock();
    }

    private void tieBlocks()
    {
        if (_previous != null)
        {
            _currentCfg.addEdge(_previous, _currentBlock);
        }
    }

    private void endBlockAndCreateEdge()
    {
        tieBlocks();
        endBlock();
    }

    public void visit(IRArrayAssign n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRArrayLength n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRArrayLookup n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRAssignment n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRCall n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRCondJump n)
    {
        // just add the statement, we'll add an edge for the jump
        // later.
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRCopy n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRLabel n)
    {
        System.out.println("label: " + n);
        System.out.println("is method: " + n.isMethod());
        // check if our current block is non-empty
        if (!_currentBlock.isEmpty())
        {
            // save the current block, since a label is always the target
            // of a branch or jump
            endBlockAndCreateEdge();

        }
        if (n.isMethod())
        {
            // if we're starting a new method, end the current control flow graph.
            if (!_currentCfg.isEmpty())
                addControlFlowGraph();

            _currentMethod = n.getMethod();
        }

        _currentBlock.addStatement(n);
        _labelsToBlocks.put(n.getLabel(), _currentBlock);
    }
    public void visit(IRNewArray n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRNewObject n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRParam n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRReturn n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();

        addControlFlowGraph();
    }
    public void visit(IRUnaryAssignment n)
    {
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();
    }
    public void visit(IRUncondJump n)
    {
        // just add the statement, we'll add an edge for the jump
        // later.
        _currentBlock.addStatement(n);
        endBlockAndCreateEdge();

        // no need to add a successor edge for fallthrough,
        // since the jump is unconditional it will always be taken.
        // so set previous to null, so no edge will be created.
        _previous = null;
    }
}