package regalloc;

import java.util.Stack;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import symboltable.*;

public class RegisterAllocator {

  private Stack<InterferenceGraphNode> _nodeStack;
  private Map<SymbolInfo, Integer> _colors;
  private Set<SymbolInfo> _spills;
  private InterferenceGraph _graph;
  private int _numRegisters;
  private int _nextColor;

  public RegisterAllocator(InterferenceGraph graph, int numRegs)
  {
    _nodeStack = new Stack<InterferenceGraphNode>();
    _colors = new HashMap<SymbolInfo, Integer>();
    _spills = new HashSet<SymbolInfo>();
    _graph = graph;
    _numRegisters = numRegs;
    _nextColor = 0;
    colorize();
  }

  private void colorize()
  {
    // simplify
    simplify();
    // select
    select();
  }

  private void simplify()
  {
    InterferenceGraphNode nextToRemove = getInsignificantNode();
    while(nextToRemove != null)
    {
      // remove node from graph
      _graph.removeNode(nextToRemove.getSymbol());
      // push to stack
      _nodeStack.push(nextToRemove);
      // get another one
      nextToRemove = getInsignificantNode();
    }

  }

  private void select()
  {
    while(_nodeStack.size() > 0)
    {
      InterferenceGraphNode node = _nodeStack.pop();
      int color = getNewRegColor(node);
      if(color >= 0)
        node.setColor(color);
      else
      {
        //uncolorable node = spill
        System.out.println("SPILLED from stack - should not have happened");
      }
      _graph.addNode(node);
    }
  }

  private int getNewRegColor(InterferenceGraphNode node)
  {
    boolean safeColor = false;
    for(int i = 0; i < _numRegisters; i++)
    {
      safeColor = true;
      for(InterferenceGraphNode neighbor : node.getInterferences())
      {
        if(neighbor.getColor() == i)
        {
          safeColor = false;
        }
      }
      if(safeColor)
        return i;
    }
    return -1;
  }

  private InterferenceGraphNode getInsignificantNode()
  {
    for(InterferenceGraphNode node : _graph.getNodes())
    {
      if(node.getDegree() < _numRegisters)
        return node;
    }
    return null;
  }

  public int getColor(SymbolInfo sym)
  {
    System.out.println("getting color for " + sym);
    return _graph.getNode(sym).getColor();
  }

  public String toString()
  {
    StringBuilder output = new StringBuilder("========= REGISTER ALLOCATIONS ========\n");
    for(InterferenceGraphNode node : _graph.getNodes())
    {
      output.append(node.getSymbolName());
      output.append(" : $");
      output.append(node.getColor());
      output.append("\n");
    }
    return output.toString();
  }
}
