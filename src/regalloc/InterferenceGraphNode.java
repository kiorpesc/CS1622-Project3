package regalloc;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import symboltable.*;
import controlflow.*;
import objectimpl.*;

public class InterferenceGraphNode {

  private SymbolInfo _symbol;
  private Set<InterferenceGraphNode> _interferences;
  private Set<InterferenceGraphNode> _moveInterferences;
  private Set<InterferenceGraphNode> _coalescedNodes;
  private boolean _moveRelated;
  private int _color;

  public InterferenceGraphNode(SymbolInfo symbol)
  {
    _symbol = symbol;
    _interferences = new HashSet<InterferenceGraphNode>();
    _moveInterferences = new HashSet<InterferenceGraphNode>();
    _coalescedNodes = new HashSet<InterferenceGraphNode>();
    _moveRelated = false;
    _color = -1;
  }

  public void addInterference(InterferenceGraphNode neighbor)
  {
    _interferences.add(neighbor);
  }

  public void addMoveInterference(InterferenceGraphNode neighbor)
  {
    _moveInterferences.add(neighbor);
    _moveRelated = true;
  }

  public void addCoalescedNode(InterferenceGraphNode friend)
  {
    _coalescedNodes.add(friend);
  }

  public boolean isMoveRelated()
  {
    return _moveRelated;
  }

  public void freezeNode()
  {
    _moveRelated = false;
  }

  public boolean interferesWith(InterferenceGraphNode nodeB)
  {
    if(_interferences.contains(nodeB) || _moveInterferences.contains(nodeB))
      return true;
    return false;
  }

  public void removeInterference(InterferenceGraphNode nodeB)
  {
    _interferences.remove(nodeB);
    _moveInterferences.remove(nodeB);
  }

  public int getDegree()
  {
    return _interferences.size();
  }

  public SymbolInfo getSymbol()
  {
    return _symbol;
  }

  public String getSymbolName()
  {
    return _symbol.getName();
  }

  public Set<InterferenceGraphNode> getInterferences()
  {
    return _interferences;
  }

  public void setColor(int color)
  {
    //set color for this and for all coalesced nodes
    _color = color;
    for(InterferenceGraphNode friend : _coalescedNodes)
    {
      friend.setColor(color);
    }
  }

  public int getColor()
  {
    return _color;
  }

  public String toString()
  {
    return _symbol.getName();
  }

}
