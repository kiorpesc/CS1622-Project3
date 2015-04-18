package regalloc;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import symboltable.*;
import controlflow.*;
import objectimpl.*;
import symboltable.*;
import irgeneration.*;

public class InterferenceGraph {

  private LivenessAnalysis _liveRanges;
  private ControlFlowGraph _cfg;
  private ObjectLayoutManager _objLayoutManager;
  private MethodSymbol _currentMethod;
  private Map<SymbolInfo, InterferenceGraphNode> _graph;

  public InterferenceGraph(LivenessAnalysis la, ControlFlowGraph cfg, ObjectLayoutManager objlm)
  {
    _liveRanges = la;
    _cfg = cfg;
    _objLayoutManager = objlm;
    _graph = new HashMap<SymbolInfo, InterferenceGraphNode>();

    buildInterferenceGraph();
  }

  // build the interference graph
  private void buildInterferenceGraph()
  {
    Set<BasicBlock> cfgBlocks = _cfg.getAllBlocks();
    for(BasicBlock block : cfgBlocks)
    {
      for(IRQuadruple statement : block.getStatements())
      {
        if(statement instanceof IRLabel)
          createThisNode((IRLabel)statement);
        if(statement instanceof IRCopy)
          processCopyStatement((IRCopy)statement);
      }
      // process live in and live out sets, build interference graph
      processLiveIn(block);
      processLiveOut(block);
    }
    patchThisLiveness();
  }

  // HACK: make this interfere with everything in a method
  private void patchThisLiveness(){
    if(_currentMethod != null)
    {
      InterferenceGraphNode thisNode = _graph.get(_currentMethod.getVariable("this"));
      for(InterferenceGraphNode nodeA : getNodes())
      {
        if(!nodeA.equals(thisNode))
        {
          addInterferenceEdge(nodeA, thisNode);
        }
      }
    }
  }

  // create a 'this' node for the current method (NOT main)
  private void createThisNode(IRLabel statement)
  {
    if(statement.isMethod() && statement.getMethod().getName() != "main")
    {
      _currentMethod = statement.getMethod();
      getOrCreateNode(statement.getMethod().getVariable("this"));
    }
  }

  private void processCopyStatement(IRCopy statement)
  {
    SymbolInfo result = statement.getResult();
    SymbolInfo arg1 = statement.getArg1();
    InterferenceGraphNode resultNode = getOrCreateNode(result);
    if(arg1 instanceof ConstantSymbol)
    {
      // nothing for now?
    } else {
      InterferenceGraphNode argNode = getOrCreateNode(arg1);
      if(!argNode.interferesWith(resultNode))
        addMoveEdge(resultNode, argNode);
    }
  }

  // process the live-in set
  private void processLiveIn(BasicBlock block)
  {
    processLiveSet(_liveRanges.getLiveIn(block));
  }

  // process the live-out set
  private void processLiveOut(BasicBlock block)
  {
    processLiveSet(_liveRanges.getLiveOut(block));
  }

  // process a live set, using combinations of values to create interferences
  private void processLiveSet(Set<SymbolInfo> liveSet)
  {
    SymbolInfo[] liveArray = liveSet.toArray(new SymbolInfo[0]);
    if(liveArray.length == 1)
      getOrCreateNode(liveArray[0]);

    for(int i = 0; i < liveArray.length - 1; i++)
    {
      InterferenceGraphNode nodeA = getOrCreateNode(liveArray[i]); // create new node
      for(int j = i+1; j < liveArray.length; j++)
      {
        InterferenceGraphNode nodeB = getOrCreateNode(liveArray[j]);
        addInterferenceEdge(nodeA, nodeB);
      }
    }
  }

  // get a node if it exists, otherwise create the node and return it
  private InterferenceGraphNode getOrCreateNode(SymbolInfo sym)
  {
    if(!_graph.containsKey(sym))
      _graph.put(sym, new InterferenceGraphNode(sym));
    return _graph.get(sym);
  }

  // add an edge between two nodes
  private void addInterferenceEdge(InterferenceGraphNode nodeA, InterferenceGraphNode nodeB)
  {
    nodeA.addInterference(nodeB);
    nodeB.addInterference(nodeA);
  }

  private void addMoveEdge(InterferenceGraphNode nodeA, InterferenceGraphNode nodeB)
  {
    nodeA.addMoveInterference(nodeB);
    nodeB.addMoveInterference(nodeA);
  }

  private void removeMoveEdge(InterferenceGraphNode nodeA, InterferenceGraphNode nodeB)
  {
    nodeA.removeMoveInterference(nodeB);
    nodeB.removeMoveInterference(nodeA);
  }

  // get a specific node
  public InterferenceGraphNode getNode(SymbolInfo sym)
  {
    return _graph.get(sym);
  }

  // ad a node to the graph, and add all of its interferences to its neighbors
  public void addNode(InterferenceGraphNode node)
  {
    _graph.put(node.getSymbol(), node);

    // add node's interferences back to other nodes
    for(InterferenceGraphNode neighbor : node.getInterferences())
    {
      neighbor.addInterference(node);
    }
  }

  // remove a specific node
  public InterferenceGraphNode removeNode(SymbolInfo symA)
  {
    // remove node interferences from remaining nodes in graph
    InterferenceGraphNode nodeA = _graph.get(symA);
    _graph.remove(symA);
    for(SymbolInfo symB : _graph.keySet())
    {
      InterferenceGraphNode nodeB = _graph.get(symB);
      if(nodeA.interferesWith(nodeB))
        nodeB.removeInterference(nodeA);

      if(nodeA.moveInterferesWith(nodeB))
        nodeB.removeMoveInterference(nodeA);
    }
    return nodeA;
  }

  // get all nodes
  public Collection<InterferenceGraphNode> getNodes()
  {
    return _graph.values();
  }

  public int getSize()
  {
    return _graph.keySet().size();
  }

  public boolean isEmpty()
  {
    return getSize() == 0;
  }

  // add all of nodeB's interferences to nodeA
  private void addAllInterferences(InterferenceGraphNode nodeA, InterferenceGraphNode nodeB)
  {
    for(InterferenceGraphNode neighbor : nodeB.getInterferences())
    {
      if(!nodeA.equals(neighbor))
        addInterferenceEdge(nodeA, neighbor);
    }
    for(InterferenceGraphNode neighbor : nodeB.getMoveInterferences())
    {
      if(!nodeA.equals(neighbor))
        addMoveEdge(nodeA, neighbor);
    }
  }

  public void coalesceNodes(InterferenceGraphNode nodeA, InterferenceGraphNode nodeB)
  {
    // add all interferences from nodeB to nodeA
    addAllInterferences(nodeA, nodeB);
    // then remove nodeB - this also removes b's interferences from the graph
    removeNode(nodeB.getSymbol());
    nodeA.removeInterference(nodeB);
    nodeA.removeMoveInterference(nodeB);
    nodeA.addCoalescedNode(nodeB);
  }

  public void freezeNode(InterferenceGraphNode nodeA)
  {
    for(InterferenceGraphNode nodeB : nodeA.getMoveInterferences())
    {
      addInterferenceEdge(nodeA, nodeB);  // the move interference is now a real interference
      removeMoveEdge(nodeA, nodeB);       // so we can remove the old move interference
    }
    nodeA.freeze();
  }

  public String toString()
  {
    StringBuilder output = new StringBuilder("================ INTERFERENCES ==============\n");
    for(SymbolInfo sym : _graph.keySet())
    {
      output.append("NODE: ");
      output.append(sym.getName());
      output.append("   :::   ");
      for(InterferenceGraphNode node : _graph.get(sym).getInterferences())
      {
        output.append(node.toString());
        output.append(" ");
      }
      output.append("\n");
    }
    output.append("================ MOVES ===============\n");
    for(SymbolInfo sym : _graph.keySet())
    {
      if(_graph.get(sym).isMoveRelated())
      {
        output.append("NODE: ");
        output.append(sym.getName());
        output.append("   :::   ");
        for(InterferenceGraphNode node : _graph.get(sym).getMoveInterferences())
        {
          output.append(node.toString());
          output.append(" ");
        }
        output.append("\n");
      }
    }
    return output.toString();
  }


}
