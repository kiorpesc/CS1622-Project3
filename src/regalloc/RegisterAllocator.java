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

    boolean freezeNow = false;
    int coalesceCount = 0;
    int iterationCount = 0;

    simplify();
    while(!_graph.isEmpty() && coalesceCount < 5)
    {
      int sizeBefore = _graph.getSize();
      coalesce();
      coalesceCount++;
      if(sizeBefore == _graph.getSize() && !_graph.isEmpty())
        freeze();
      simplify();
    }
    int colorized = select();
    if(colorized == _graph.getSize())
    {
      System.out.println("Colored all nodes!");
    } else {
      System.out.println("Attempting to color potential spills");
      int actualSpills = tryToColorPotentialSpills();
      if(actualSpills != 0)
      {
        System.out.println("SPILL OCCURRED, exiting...");
        System.exit(1);
      }
    }
  }

  private int tryToColorPotentialSpills()
  {
    int actualSpills = 0;
    int color = -1;
    for(InterferenceGraphNode node : _graph.getNodes())
    {
      if(!_colors.containsKey(node.getSymbol()))
      {
        color = getNewRegColor(node);
        if(color < 0)
        {
          actualSpills++;
        } else {
          node.setColor(color, _colors);
        }
      }
    }
    return actualSpills;
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

  private void coalesce()
  {
    System.out.println("COALESCE");
    boolean coalesced = true;
    while(coalesced)
    {
      coalesced = _coalesce();
    }
  }

  // use George coalesce
  private boolean canCoalesce(InterferenceGraphNode nodeA, InterferenceGraphNode nodeB)
  {
    boolean safeToCoalesce = true;

    if(nodeA.interferesWith(nodeB))
    {
      return false;
    }

    for(InterferenceGraphNode neighbor : nodeA.getInterferences())
    {
        if(nodeB.interferesWith(neighbor) || (neighbor.getDegree() < _numRegisters))
        {
          // this is safe
        }
        else
        {
          safeToCoalesce = false;
        }
    }
    return safeToCoalesce;
  }

  private boolean _coalesce()
  {
    for(InterferenceGraphNode nodeA : _graph.getNodes())
    {
      if(nodeA.isMoveRelated())
      {
        for(InterferenceGraphNode nodeB : nodeA.getMoveInterferences())
        {
          // if nodes can be coalesced, do so
          if(canCoalesce(nodeA, nodeB))
          {
            System.out.println("Combining " + nodeA.getSymbolName() + " and " + nodeB.getSymbolName());
            _graph.coalesceNodes(nodeA, nodeB);
            return true;
          }
        }
      }
    }
    return false;
  }

  private void freeze()
  {
    InterferenceGraphNode nodeToFreeze = getNodeToFreeze();
    if(nodeToFreeze != null)
      _graph.freezeNode(nodeToFreeze);
    else
      System.out.println("UNABLE TO FREEZE NODE - SPILLTACULAR");
  }

  private InterferenceGraphNode getNodeToFreeze()
  {
    for(InterferenceGraphNode node : _graph.getNodes())
    {
      if(node.isMoveRelated() && node.getDegree() < _numRegisters)
        return node;
    }
    return null;
  }

  private int select()
  {
    int colorized = 0;
    while(_nodeStack.size() > 0)
    {
      InterferenceGraphNode node = _nodeStack.pop();
      int color = getNewRegColor(node);
      if(color >= 0)
      {
        node.setColor(color, _colors);
        colorized++;
      }
      else
      {
        //uncolorable node = spill
        System.out.println("SPILLED from stack - should not have happened");
      }
      _graph.addNode(node);
    }
    return colorized;
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
      if(node.getDegree() < _numRegisters && !node.isMoveRelated())
        return node;
    }
    return null;
  }

  public int getColor(SymbolInfo sym)
  {
    System.out.println("Getting color for " + sym.getName());
    return _colors.get(sym);
  }

  public String toString()
  {
    StringBuilder output = new StringBuilder("========= REGISTER ALLOCATIONS ========\n");
    for(SymbolInfo nodeSymbol : _colors.keySet())
    {
      output.append(nodeSymbol.getName());
      output.append(" : $");
      output.append(_colors.get(nodeSymbol));
      output.append("\n");
    }
    return output.toString();
  }
}
