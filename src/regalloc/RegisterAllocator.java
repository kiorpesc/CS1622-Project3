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
  private int _numRegisters;
  private int _nextColor;

  public RegisterAllocator(InterferenceGraph graph, int numRegs)
  {
    _nodeStack = new Stack<SymbolInfo>();
    _nodes = new HashSet(graph.getNodes()); // make a copy of the keySet
    _colors = new HashMap<SymbolInfo, Integer>();
    _spills = new HashSet<SymbolInfo>();
    _graph = graph;
    _numRegisters = numRegs;
    _nextColor = 0;

    colorize();
  }

  public void colorize()
  {
    // simplify
    simplify();
    printNodeStack();
    // select
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
    SymbolInfo nextToRemove = getInsignificantNode();
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
      nextToRemove = getInsignificantNode();
    }

    if(_nodes.size() > 0)
    {
      // we could not simplify all the way, these nodes are potential spills
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
        // ACTUAL SPILL - need to rewrite source code IR
      } else {
        // remove register from spills list, give color
        _spills.remove(nextToAdd);
        _colors.put(nextToAdd, color);
        _nodes.add(nextToAdd);
      }

      // TODO: should this be done for spills, or are they now ALWAYS in mem and therefore not on the graph?
      for(SymbolInfo nodeA : _graph.getInterferences(nextToAdd))
      {
        _graph.addInterference(nodeA, nextToAdd);
      }
    }
  }

  private int getNewRegColor(SymbolInfo node)
  {
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
        if(reg == _colors.get(adjNode)){
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

  private SymbolInfo getInsignificantNode()
  {
    for(SymbolInfo sym : _nodes)
    {
      if(_graph.getDegree(sym) < _numRegisters)
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
}
