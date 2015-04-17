package regalloc;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import controlflow.*;
import symboltable.*;

public class InterferenceGraph {

    private Map<SymbolInfo, Set<SymbolInfo>> _graph;
    private Map<SymbolInfo, Set<SymbolInfo>> _moves;
    private LivenessAnalysis _liveRanges;
    private ControlFlowGraph _cfg;

    public InterferenceGraph(LivenessAnalysis la, ControlFlowGraph cfg)
    {
      _graph = new HashMap<SymbolInfo, Set<SymbolInfo>>();
      _moves = new HashMap<SymbolInfo, SymbolInfo>();
      _liveRanges = la;
      _cfg = cfg;

      buildInterferenceGraph();
    }

    public void buildInterferenceGraph()
    {
      Set<BasicBlock> cfgBlocks = _cfg.getAllBlocks();
      for(BasicBlock block : cfgBlocks){
        for(IRQuadruple ir : block.getStatements())
        {
          if(ir instanceOf IRCopy)
          {
            checkCopyInterference(ir);
          }
        }
        processLiveIn(block);
        processLiveOut(block);
      }
    }

    private void checkCopyInterference(IRCopy copyIR)
    {
      SymbolInfo result = copyIR.getResult();
      SymbolInfo arg = copyIR.getArg1();
      if(arg instanceOf ConstantSymbol)
      {
        // do nothing
      } else {
        addMoveInterference(result, arg);
        addMoveInterference(arg, result);
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

    public void coalesceNodes(SymbolInfo a, SymbolInfo b)
    {
      // ?
    }

    private void processLiveIn(BasicBlock block)
    {
      processLiveSet(_liveRanges.getLiveIn(block));
    }

    private void processLiveOut(BasicBlock block)
    {
      processLiveSet(_liveRanges.getLiveOut(block));
    }

    private void processLiveSet(Set<SymbolInfo> liveSet)
    {
      Object[] liveArray = liveSet.toArray();

      // if there are no interferences, we still need to allocate for the variable
      if(liveArray.length == 1 && !_graph.containsKey(liveArray[0]))
      {
        _graph.put((SymbolInfo)liveArray[0], new HashSet<SymbolInfo>());
      }

      for(int i = 0; i < liveArray.length - 1; i++)
      {
        SymbolInfo symA = (SymbolInfo)liveArray[i];

        for(int j = i + 1; j < liveArray.length; j++)
        {
          SymbolInfo symB = (SymbolInfo)liveArray[j];
          addEdge(symA, symB);
        }
      }
    }

    public void addInterference(SymbolInfo a, SymbolInfo b)
    {
      if(!_graph.containsKey(a))
      {
        _graph.put(a, new HashSet<SymbolInfo>());
      }
      _graph.get(a).add(b);
    }

    public void removeInterference(SymbolInfo a, SymbolInfo b)
    {
      Set<SymbolInfo> aInterferences = _graph.get(a);
      aInterferences.remove(b);
    }

    public void addEdge(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      if(!_moves.get(nodeA).equals(nodeB)) // dont add move interferences
      {
        addInterference(nodeA, nodeB);
        addInterference(nodeB, nodeA);
      }
    }

    public void removeEdge(SymbolInfo nodeA, SymbolInfo nodeB)
    {
      removeInterference(nodeA, nodeB);
      removeInterference(nodeB, nodeA);
    }

    public int getDegree(SymbolInfo sym)
    {
      return _graph.get(sym).size();
    }

    public Set<SymbolInfo> getNodes()
    {
      return _graph.keySet();
    }

    public Map<SymbolInfo, Set<SymbolInfo>> getMoves()
    {
      return _moves;
    }

    public Set<SymbolInfo> getInterferences(SymbolInfo a)
    {
      Set<SymbolInfo> interferences = _graph.get(a);
      return interferences;
    }

    public String toString()
    {
      StringBuilder output = new StringBuilder("========== INTERFERENCES: =========\n");
      for(SymbolInfo key : _graph.keySet())
      {
        output.append("NODE: ");
        output.append(key.getName());
        output.append("  :::  ");
        for(SymbolInfo n : _graph.get(key))
          output.append(n.getName() + ", ");
        output.append("\n");
      }
      return output.toString();
    }
}
