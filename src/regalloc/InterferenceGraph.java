package regalloc;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import controlflow.*;
import symboltable.*;
import irgeneration.*;
import objectimpl.*;

public class InterferenceGraph {

    private Map<SymbolInfo, Set<SymbolInfo>> _graph;
    private Map<SymbolInfo, Set<SymbolInfo>> _moves;
    private Set<SymbolInfo> _nodes; // this is the publicly visible set of non-move nodes CURRENTLY in the graph
    private Set<SymbolInfo> _moveNodes; // this is the publicly visible set of move nodes CURRENTLY in the graph
    private LivenessAnalysis _liveRanges;
    private ControlFlowGraph _cfg;
    private ObjectLayoutManager _objLayoutManager;
    private MethodSymbol _currentMethod;

    public InterferenceGraph(LivenessAnalysis la, ControlFlowGraph cfg, ObjectLayoutManager objlm)
    {
      _graph = new HashMap<SymbolInfo, Set<SymbolInfo>>();
      _moves = new HashMap<SymbolInfo, Set<SymbolInfo>>();
      _nodes = new HashSet<SymbolInfo>();
      _moveNodes = new HashSet<SymbolInfo>();
      _liveRanges = la;
      _cfg = cfg;
      _objLayoutManager = objlm;

      buildInterferenceGraph();
    }

    public void buildInterferenceGraph()
    {
      Set<BasicBlock> cfgBlocks = _cfg.getAllBlocks();
      for(BasicBlock block : cfgBlocks){               // for each block in the control flow graph
        for(IRQuadruple ir : block.getStatements())    // check whether there are any copies
        {
          if(ir instanceof IRLabel)
          {
            if(((IRLabel)ir).isMethod())
              _currentMethod = ((IRLabel)ir).getMethod();
          }
          if(ir instanceof IRCopy)
          {
            checkCopyInterference((IRCopy)ir); // if there are move-related interferences, add them to the move graph
          }
        }
        processLiveIn(block);   // add regular interferences to the interference graph
        processLiveOut(block);
      }
      patchInstanceVariables();
    }

    private void patchInstanceVariables()
    {
      for(SymbolInfo var : _graph.keySet())
      {
        if(_objLayoutManager.isInstanceVariable(var))
        {
          _graph.get(var).add(_currentMethod.getVariable("this"));
        }
      }
      for(SymbolInfo var : _moves.keySet())
      {
        if(_objLayoutManager.isInstanceVariable(var))
        {
          addEdge(var, _currentMethod.getVariable("this"));
        }
      }
    }

    private void checkCopyInterference(IRCopy copyIR)
    {
      SymbolInfo result = copyIR.getResult();
      SymbolInfo arg = copyIR.getArg1();
      if(arg instanceof ConstantSymbol)
      {
        // NOT a copy, BUT result still needs to be processed
        if(!_graph.containsKey(result))
        {
          _graph.put(result, new HashSet<SymbolInfo>());
          addNode(result);
        }
      } else {
        addMoveInterference(result, arg);
        addMoveInterference(arg, result);
        addMoveNode(result);
        addMoveNode(arg);
      }
    }

    public void addMoveInterference(SymbolInfo a, SymbolInfo b)
    {
      if(!_moves.containsKey(a))
      {
        _moves.put(a, new HashSet<SymbolInfo>());
      }
      _moves.get(a).add(b);
    }

    public void removeMoveInterference(SymbolInfo a, SymbolInfo b)
    {
      Set<SymbolInfo> aInterferences = _moves.get(a);
      aInterferences.remove(b);
    }

    private void replaceInterferences(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      for(SymbolInfo key : _graph.keySet())
      {
        Set<SymbolInfo> neighbors = new HashSet<SymbolInfo>(_graph.get(key)); // make copy
        for(SymbolInfo neighbor : neighbors)
        {
          if(neighbor.equals(nodeA))
          {
            _graph.get(key).remove(nodeA);
            _graph.get(key).add(nodeB);
          }
        }
      }
    }

    private void replaceMoveInterferences(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      for(SymbolInfo key : _moves.keySet())
      {
        Set<SymbolInfo> moves = new HashSet<SymbolInfo>(_moves.get(key));
        for(SymbolInfo move : moves)
        {
          if(move.equals(nodeA))
          {
            _moves.get(key).remove(nodeA);
            _moves.get(key).add(nodeB);
          }
        }
      }
    }

    // add all of nodeB's interferences to nodeA
    public void addAllInterferences(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      Set<SymbolInfo> aNeighbors = _graph.get(nodeA);
      Set<SymbolInfo> bNeighbors = _graph.get(nodeB);
      if(aNeighbors == null)
      {
        aNeighbors = new HashSet<SymbolInfo>();
        _graph.put(nodeA, aNeighbors);
      }
      if(bNeighbors != null)
      {
        aNeighbors.addAll(bNeighbors);
        // any nodes that had nodeB as an interference now need to have nodeA
        replaceInterferences(nodeB, nodeA);

      }
      aNeighbors.remove(nodeA); // remove trivial interferences
      aNeighbors.remove(nodeB);
    }

    // add all of nodeB's move interferences to nodeA
    public void addAllMoveInterferences(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      Set<SymbolInfo> aMoves = _moves.get(nodeA);
      Set<SymbolInfo> bMoves = _moves.get(nodeB);
      if(aMoves == null)
      {
        aMoves = new HashSet<SymbolInfo>();
        _moves.put(nodeA, aMoves);
      }
      if(bMoves != null)
      {
        aMoves.addAll(bMoves);
        // any nodes that had nodeB as an interference now need to have nodeA
        replaceMoveInterferences(nodeB, nodeA);
      }
      aMoves.remove(nodeA); // remove trivial interferences
      aMoves.remove(nodeB);
    }

    public void coalesceNodes(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      System.out.println("combining " + nodeA.getName() + " and " + nodeB.getName());
      Set<SymbolInfo> bNeighbors = _graph.get(nodeB);
      Set<SymbolInfo> bMoves = _moves.get(nodeB);

      addAllInterferences(nodeA, nodeB);
      addAllMoveInterferences(nodeA, nodeB);

      // now remove nodeB from the graph entirely
      removeNode(nodeB);
      removeMoveNode(nodeB);

      // if a no longer has move interferences, mark it as non-move
      if(_moves.get(nodeA).size() == 0)
      {
        _moves.remove(nodeA); //remove node from _moves map
        removeMoveNode(nodeA);  // remove node from set of movenodes
        addNode(nodeA);       // add node to non-move set
      }
    }

    private void processLiveIn(BasicBlock block)
    {
      processLiveSet(_liveRanges.getLiveIn(block));
    }

    private void processLiveOut(BasicBlock block)
    {
      processLiveSet(_liveRanges.getLiveOut(block));
    }

    // walks the live-in or live-out set, adding interferences
    private void processLiveSet(Set<SymbolInfo> liveSet)
    {
      Object[] liveArray = liveSet.toArray();

      // if there are no interferences, we still need to allocate for the variable
      if(liveArray.length == 1 && !_graph.containsKey(liveArray[0]))
      {
        _graph.put((SymbolInfo)liveArray[0], new HashSet<SymbolInfo>());
        _nodes.add((SymbolInfo)liveArray[0]);
      }

      for(int i = 0; i < liveArray.length - 1; i++)
      {
        SymbolInfo symA = (SymbolInfo)liveArray[i];

        for(int j = i + 1; j < liveArray.length; j++)
        {
          SymbolInfo symB = (SymbolInfo)liveArray[j];
          //if(!_moves.containsKey(symA) || !_moves.get(symA).contains(symB))        // only add interference if it is not a move
          //{
            addEdge(symA, symB);
          //}
        }
      }
    }

    // add node b to node a's interferences
    public void addInterference(SymbolInfo a, SymbolInfo b)
    {
      if(!_graph.containsKey(a))
      {
        _graph.put(a, new HashSet<SymbolInfo>());
      }
      _graph.get(a).add(b);
    }

    // remove node b from node a's interferences
    public void removeInterference(SymbolInfo a, SymbolInfo b)
    {
      Set<SymbolInfo> aInterferences = _graph.get(a);
      aInterferences.remove(b);
    }

    // create an edge between two nodes
    public void addEdge(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      addInterference(nodeA, nodeB);
      addInterference(nodeB, nodeA);
      addNode(nodeA);
      addNode(nodeB);
    }

    // add a node to the node list (independent of the Maps)
    public void addNode(SymbolInfo node)
    {
      if(node == null)
        System.out.println("adding null node to set");
      _nodes.add(node);
    }

    public void removeNode(SymbolInfo node)
    {
      _nodes.remove(node);
    }

    public void addMoveNode(SymbolInfo node)
    {
      _moveNodes.add(node);
    }

    public void removeMoveNode(SymbolInfo node)
    {
      _moveNodes.remove(node);
    }

    public void freezeNode(SymbolInfo node)
    {
      // mark node as non-move related
      removeMoveNode(node);
      addNode(node);
    }

    // remove an interference edge
    public void removeEdge(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      removeInterference(nodeA, nodeB);
      removeInterference(nodeB, nodeA);
    }

    // get the non-move degree of the symbol
    public int getDegree(SymbolInfo sym)
    {
      Set<SymbolInfo> neighbors = _graph.get(sym);
      if(neighbors == null)
        return 0;
      return neighbors.size();
    }

    public int getMoveDegree(SymbolInfo sym)
    {
      Set<SymbolInfo> moves = _moves.get(sym);
      if(moves == null)
        return 0;
      return moves.size();
    }

    // get the list of nodes
    public Set<SymbolInfo> getNodes()
    {
      return _nodes;
    }

    // get the map of nodes to move interferences
    public Map<SymbolInfo, Set<SymbolInfo>> getMoves()
    {
      return _moves;
    }
    // get the set of move interferences for symbol a
    public Set<SymbolInfo> getMoves(SymbolInfo a)
    {
      return _moves.get(a);
    }

    public Set<SymbolInfo> getMoveNodes()
    {
      return _moveNodes;
    }

    // get the set of interferences for symbol a
    public Set<SymbolInfo> getInterferences(SymbolInfo a)
    {
      Set<SymbolInfo> interferences = _graph.get(a);
      return interferences;
    }

    public Map<SymbolInfo, Set<SymbolInfo>> getAllInterferences()
    {
      return _graph;
    }

    public int getTotalSize()
    {
      //return _nodes.size() + _moves.size();
      return _nodes.size();
    }

    public boolean isEmpty()
    {
      return getTotalSize() == 0;
    }

    public String toString()
    {

      //System.out.println(_nodes);
      //System.out.println(_moveNodes);

      StringBuilder output = new StringBuilder("========== INTERFERENCES: =========\n");
      for(SymbolInfo key : _nodes)
      {
        output.append("NODE: ");
        output.append(key.getName());
        output.append("  :::  ");
        for(SymbolInfo n : _graph.get(key))
          output.append(n.getName() + ", ");
        output.append("\n");
      }
      output.append("============ MOVES ===========\n");
      for(SymbolInfo key : _moveNodes)
      {
        output.append("NODE: ");
        output.append(key.getName());
        output.append("  :::  ");
        for(SymbolInfo n : _moves.get(key))
          output.append(n.getName() + ", ");
        output.append("\n");
      }
      return output.toString();
    }
}
