package controlflow;

import java.util.*;

public class ControlFlowGraph
{
    private Map<BasicBlock, Set<BasicBlock>> _adjacencyList;
    private Map<BasicBlock, Set<BasicBlock>> _predecessors;
    private BasicBlock _root;

    public ControlFlowGraph()
    {
        _adjacencyList = new HashMap<BasicBlock, Set<BasicBlock>>();
        _predecessors = new HashMap<BasicBlock, Set<BasicBlock>>();
        _root = null;
    }

    public boolean isEmpty()
    {
        return _root == null;
    }

    public BasicBlock getRoot()
    {
        return _root;
    }

    public void addBlock(BasicBlock b)
    {
        if (b.isEmpty())
            throw new IllegalArgumentException("basic block cannot be empty");

        if (_adjacencyList.containsKey(b))
            throw new IllegalArgumentException("duplicate block found");

        if (_root == null)
            _root = b;

        _adjacencyList.put(b, new HashSet<BasicBlock>());
        if (!_predecessors.containsKey(b))
        {
            _predecessors.put(b, new HashSet<BasicBlock>());
        }
    }

    public void addEdge(BasicBlock from, BasicBlock to)
    {
        _adjacencyList.get(from).add(to);

        Set<BasicBlock> preds = _predecessors.get(to);
        // TODO: hack because we add a predecessor edge before we
        // actually add the to block.
        if (preds == null)
        {
            preds = new HashSet<BasicBlock>();
            _predecessors.put(to, preds);
        }
        preds.add(from);
    }

    public Set<BasicBlock> getPredecessors(BasicBlock to)
    {
        return _predecessors.get(to);
    }

    public Set<BasicBlock> getSuccessors(BasicBlock from)
    {
        return _adjacencyList.get(from);
    }

    public Set<BasicBlock> getAllBlocks()
    {
        return _adjacencyList.keySet();
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        for (BasicBlock b : getAllBlocks())
        {
            result.append(b.toString());
            result.append("goes to {\n");
            for (BasicBlock succ : getSuccessors(b))
            {
                result.append(succ.toString(1));
            }
            result.append("}\n");
        }

        return result.toString();
    }
}
