package controlflow;

import java.util.*;

public class ControlFlowGraph
{
    private Map<BasicBlock, Set<BasicBlock>> _adjacencyList;

    public ControlFlowGraph()
    {
        _adjacencyList = new HashMap<BasicBlock, Set<BasicBlock>>();
    }

    public void addBlock(BasicBlock b)
    {
        if (_adjacencyList.containsKey(b))
            throw new IllegalArgumentException("duplicate block found");

        _adjacencyList.put(b, new HashSet<BasicBlock>());
    }

    public void addEdge(BasicBlock from, BasicBlock to)
    {
        _adjacencyList.get(from).add(to);
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
