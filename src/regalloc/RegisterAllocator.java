package regalloc;

import symboltable.*;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Stack;

public class RegisterAllocator {

  private Stack<SymbolInfo> _nodeStack;
  private Map<SymbolInfo, Integer> _colors;
  private Set<SymbolInfo> _spills;
  private InterferenceGraph _graph;
  private Map<SymbolInfo, Set<SymbolInfo>> _coalesced;
  private int _numRegisters;
  private int _nextColor;

  public RegisterAllocator(InterferenceGraph graph, int numRegs)
  {
    _nodeStack = new Stack<SymbolInfo>();
    _colors = new HashMap<SymbolInfo, Integer>();
    _spills = new HashSet<SymbolInfo>();
    _coalesced = new HashMap<SymbolInfo, Set<SymbolInfo>>();
    _graph = graph;
    _numRegisters = numRegs;
    _nextColor = 0;
    colorize();
  }

  public void colorize()
  {
    boolean freezeNow = false;
    int coalesceCount = 0;
    int iterationCount = 0;

    while(_graph.getNodes().size() != 0){ // potential spills
      while(!freezeNow)
      {
        simplify();
        if(_graph.getNodes().size() != 0)
          coalesce();

        if(coalesceCount == 5)
          freezeNow = true;

        coalesceCount++;
      }
      //freeze(); // pick low-degree move node and mark it as non-move, then loop back
    }
    select();
  }

  private void printNodeStack()
  {
    System.out.println("Printing node stack ........");
    for(SymbolInfo node : _nodeStack)
    {
      System.out.println(node);
    }
  }

  private void simplify()
  {
    System.out.println("SIMPLIFY");
    SymbolInfo nextToRemove = getInsignificantNonMoveNode();
    // repeat until no nodes left, or until we can't find an insignificant node
    while(_graph.getNodes().size() > 0 && nextToRemove != null)
    {
      // remove node and put on stack
      // first, remove interferences to other nodes (without removing them from the node itself)
      Set<SymbolInfo> nextInterferences = new HashSet<SymbolInfo>(_graph.getInterferences(nextToRemove));
      for(SymbolInfo nodeA : nextInterferences)
        _graph.removeInterference(nodeA, nextToRemove);

      _nodeStack.push(nextToRemove); // push to the stack
      _graph.removeNode(nextToRemove);

      // pick next node
      nextToRemove = getInsignificantNonMoveNode();
    }
  }

  // criteria set by George coalescing algorithm
  // for every neighbor t of a:
  //    t already interferes with b
  //    OR t is of insignificant degree
  private boolean canCombine(SymbolInfo nodeA, SymbolInfo nodeB)
  {
    Set<SymbolInfo> aNeighbors = _graph.getInterferences(nodeA);
    Set<SymbolInfo> bNeighbors = _graph.getInterferences(nodeB);
    for(SymbolInfo t : aNeighbors)
    {
      if(!bNeighbors.contains(t))     // if t does not interfere with b, check the second condition
      {
        if(_graph.getDegree(t) >= _numRegisters)  // if t is of significant degree AND not a neighbor, we can't combine
          return false;
      }
    }
    return true;
  }

  private boolean combine(SymbolInfo nodeA, SymbolInfo nodeB)
  {
    System.out.println("combining " + nodeA.getName() + " and " + nodeB.getName() );
    // add all of b's interferences (move or otherwise) to a
    _graph.coalesceNodes(nodeA, nodeB);

    // somewhere store the link between these two nodes
    if(!_coalesced.containsKey(nodeA))
    {
      _coalesced.put(nodeA, new HashSet<SymbolInfo>());
    }
    _coalesced.get(nodeA).add(nodeB);

    return true;
  }

  // George coalescing
  private boolean _coalesce()
  {
    boolean canCombine;
    for(SymbolInfo nodeA : _graph.getMoveNodes())
    {
      Set<SymbolInfo> aMoves = new HashSet(_graph.getMoves(nodeA)); // get a COPY of the set of move interferences from this node
      for(SymbolInfo nodeB : aMoves)
      {
        if(canCombine(nodeA, nodeB))
        {
          return combine(nodeA, nodeB);
        }
      }
    }
    return false;
  }

  private void coalesce()
  {
    System.out.println("COALESCE");
    boolean coalesced = true;
    while(coalesced)
    {
      coalesced = _coalesce();
    }
  }

  // TODO: what else needs to be done when a node is marked non-move?
  private void freeze()
  {
    SymbolInfo moveNode = getNodeToFreeze();
    _graph.freezeNode(moveNode);
  }

  private SymbolInfo getNodeToFreeze()
  {
    for(SymbolInfo moveNode : _graph.getMoveNodes())
    {
      if(_graph.getDegree(moveNode) < _numRegisters)
      {
        return moveNode;
      }
    }
    return null;
  }

  private void select()
  {
    SymbolInfo nextToAdd;
    while(_nodeStack.size() > 0)
    {
      // pop node, give color, save in set
      // for each of its interferences, add that interference back to the already popped nodes
      nextToAdd = _nodeStack.pop();

      // check color of all neighbors
      int color = getNewRegColor(nextToAdd);
      if(color < 0) // this means no safe color was found
      {
        // since this node is from the stack, this should not occur - potential spills were not on the stack
        System.out.println("Spill occurred from STACK - this should not occur.");
      } else {
        _colors.put(nextToAdd, color);
        if(_coalesced.containsKey(nextToAdd)) // colorize any nodes that have been combined into this one
        {
          for(SymbolInfo copy : _coalesced.get(nextToAdd))
            _colors.put(copy, color);
        }
        _graph.addNode(nextToAdd); // add the node back to the graph
      }

      for(SymbolInfo nodeA : _graph.getInterferences(nextToAdd))
      {
        _graph.addInterference(nodeA, nextToAdd);
      }
    }
    /*
    // now check to see if we can colorize the potential spills
    Set<SymbolInfo> tempSpillSet = new HashSet<SymbolInfo>(_spills); // copy the set to prevent ordering issues when removing
    for(SymbolInfo potentialSpill : tempSpillSet)
    {
      int color = getNewRegColor(potentialSpill);
      if(color >= 0)
      {
        // not an actual spill
        _spills.remove(potentialSpill);
        _colors.put(potentialSpill, color);
        _nodes.add(nextToAdd);
      }
      // shouldn't need to add any interferences, as they were never removed
    }
    */
  }

  private int getNewRegColor(SymbolInfo node)
  {
    System.out.println("getting color for " + node.getName());
    Set<SymbolInfo> interferences = _graph.getInterferences(node);
    if(interferences == null){
      return 0;
    }

    boolean safeColor = false;
    // check each adjacent node
    for(int reg = 0; reg < _numRegisters; reg++)
    {
      safeColor = true;
      for(SymbolInfo adjNode : interferences)
      {
        Integer adjColor = _colors.get(adjNode);
        if(adjColor != null && reg == adjColor){
          safeColor = false;
        }
      }
      if(safeColor)
      {
        return reg;
      }
    }
    // TODO: if there are no colors, there will be blood
    return -1;
  }

  private SymbolInfo getInsignificantNonMoveNode()
  {
    for(SymbolInfo sym : _graph.getNodes())
    {
      if(_graph.getDegree(sym) < _numRegisters && !_graph.getMoveNodes().contains(sym))
        return sym;
    }
    return null;
  }

  public String toString()
  {
    StringBuilder output = new StringBuilder("============ Register Allocations ==========\n");
    for(SymbolInfo node : _graph.getNodes())
    {
      output.append(node.getName());
      output.append(" : $");
      output.append(_colors.get(node));
      output.append("\n");
    }
    return output.toString();
  }

  public int getRegister(SymbolInfo var)
  {
    return _colors.get(var);
  }

  public Map<SymbolInfo, Integer> getColors()
  {
    return _colors;
  }

}
