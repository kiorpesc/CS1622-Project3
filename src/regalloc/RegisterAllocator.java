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
  boolean coalesced = false;
  boolean simplified = false;
  boolean frozen = false;

  while(!_graph.isEmpty())
  {
    simplified = false;
    coalesced = false;
    simplified = simplify();
    if(!_graph.isEmpty())
      coalesced = coalesce();

    if(!simplified && !coalesced)
    {
      frozen = freeze();
      if(!frozen)
        spill();
    }
  }
  select();
  int spills = tryToColorPotentialSpills();
  if(spills != 0)
  {
    System.out.println("An actual SPILL occurred, unable to continue.");
    System.exit(0);
  }
}

  private void spill()
  {
    //System.out.println("SPILL");
    InterferenceGraphNode toSpill = null;
    for(InterferenceGraphNode node : _graph.getNodes())
    {
      // any spill will do, but i needed an iterator to grab shit out of a set
      toSpill = node;
      break;
    }
    if(toSpill != null) {
      toSpill.spill();
      _graph.removeNode(toSpill.getSymbol());
      _nodeStack.push(toSpill);
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

  private boolean simplify()
  {
    //System.out.println("SIMPLIFY");
    boolean simplified = false;
    InterferenceGraphNode nextToRemove = getInsignificantNode();
    while(nextToRemove != null)
    {
      // remove node from graph
      _graph.removeNode(nextToRemove.getSymbol());
      // push to stack
      _nodeStack.push(nextToRemove);
      simplified = true;
      // get another one
      nextToRemove = getInsignificantNode();
    }
    return simplified;
  }

  private boolean coalesce()
  {
    int coalesceCount = 0;
    //System.out.println("COALESCE");
    boolean coalesced = true;
    while(coalesced)
    {
      coalesced = _coalesce();
      if(coalesced)
        coalesceCount++;
    }
    return coalesceCount > 0;
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
            //System.out.println("Combining " + nodeA.getSymbolName() + " and " + nodeB.getSymbolName());
            _graph.coalesceNodes(nodeA, nodeB);
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean freeze()
  {
    //System.out.println("FREEZE");
    InterferenceGraphNode nodeToFreeze = getNodeToFreeze();
    if(nodeToFreeze != null){
      _graph.freezeNode(nodeToFreeze);
      return true;
    }
    else
      return false;
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
      if(!node.isSpilled()) // if its a spill, just add it back without coloring it
      {
        int color = getNewRegColor(node);
        if(color >= 0)
        {
          node.setColor(color, _colors);
          colorized++;
        }
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
    //System.out.println("Getting color for " + sym.getName());
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
