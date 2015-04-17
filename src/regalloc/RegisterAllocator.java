package regalloc;

import symboltable.*;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Stack;

public class RegisterAllocator {

  private Stack<SymbolInfo> _nodeStack;
  private Set<SymbolInfo> _nodes;
  private Map<SymbolInfo, Integer> _colors;
  private Set<SymbolInfo> _spills;
  private InterferenceGraph _graph;
  private Map<SymbolInfo, Set<SymbolInfo>> _moves;
  private int _numRegisters;
  private int _nextColor;

  public RegisterAllocator(InterferenceGraph graph, int numRegs)
  {
    _nodeStack = new Stack<SymbolInfo>();
    _nodes = new HashSet(graph.getNodes()); // make a copy of the keySet
    _colors = new HashMap<SymbolInfo, Integer>();
    _spills = new HashSet<SymbolInfo>();
    _graph = graph;
    _moves = graph.getMoves();
    _numRegisters = numRegs;
    _nextColor = 0;

    colorize();
  }

  public void colorize()
  {
    boolean freezeNow = false;
    int coalesceCount = 0;
    int iterationCount = 0;

    while(_nodes.size() != 0){ // potential spills
      while(!freezeNow)
      {
        simplify();
        if(_nodes.size() != 0)
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
    SymbolInfo nextToRemove = getInsignificantNonMoveNode();
    // repeat until no nodes left, or until we can't find an insignificant node
    while(_nodes.size() > 0 && nextToRemove != null)
    {
      // remove node and put on stack
      // first, remove interferences to other nodes (without removing them from the node itself)
      for(SymbolInfo nodeA : _graph.getInterferences(nextToRemove))
        _graph.removeInterference(nodeA, nextToRemove);

      _nodeStack.push(nextToRemove); // push to the stack
      _nodes.remove(nextToRemove);  // remove it from the list

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
    Set<SymbolInfo> aNeighbors = _graph.get(nodeA);
    Set<SymbolInfo> bNeighbors = _graph.get(nodeB);
    for(SymbolInfo t : aNeighbors)
    {
      if(!bNeighbors.contains(t))     // if t does not interfere with b, check the second condition
      {
        if(_graph.get(t) >= _numRegisters)  // if t is of significant degree AND not a neighbor, we can't combine
          return false;
      }
    }
    return true;
  }

  // George coalescing
  private void coalesce()
  {
    boolean canCombine;
    for(SymbolInfo nodeA : _moves.keySet())
    {
      Set<SymbolInfo> aMoves = new HashSet(_moves.get(nodeA)); // get a COPY of the set of move interferences from this node
      for(SymbolInfo nodeB : aMoves)
      {
        if(canCombine(nodeA, nodeB))
        {
          combine(nodeA, nodeB);
        }
      }
    }
  }

  private void freeze()
  {
    SymbolInfo moveNode = getNodeToFreeze();
    _moves.remove(moveNode);
  }

  private SymbolInfo getNodeToFreeze()
  {
    for(SymbolInfo moveNode : _moves)
    {
      if(_graph.getDegree(moveNode) < _numRegisters)
      {
        return moveNode;
      }
    }
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
        _nodes.add(nextToAdd);
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
    for(SymbolInfo sym : _nodes)
    {
      if(_graph.getDegree(sym) < _numRegisters && !_moves.containsKey(sym))
        return sym;
    }
    return null;
  }

  public String toString()
  {
    StringBuilder output = new StringBuilder("============ Register Allocations ==========\n");
    for(SymbolInfo node : _nodes)
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
