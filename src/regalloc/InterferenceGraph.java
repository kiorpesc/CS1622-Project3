package regalloc;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import controlflow.*;
import symboltable.*;

public class InterferenceGraph {

    private Map<SymbolInfo, Set<SymbolInfo>> _graph;
    private LivenessAnalysis _liveRanges;
    private ControlFlowGraph _cfg;

    public InterferenceGraph(LivenessAnalysis la, ControlFlowGraph cfg)
    {
      _graph = new HashMap<SymbolInfo, Set<SymbolInfo>>();
      _liveRanges = la;
      _cfg = cfg;

      buildInterferenceGraph();
    }

    public void buildInterferenceGraph()
    {
      for(BasicBlock block : _cfg.getAllBlocks()){
        processLiveIn(block);
        processLiveOut(block);
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

    private void processLiveSet(Set<SymbolInfo> liveSet)
    {
      Object[] liveArray = liveSet.toArray();
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
      addInterference(nodeA, nodeB);
      addInterference(nodeB, nodeA);
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
        output.append("NODE:    ");
        output.append(key.getName());
        output.append(" ::: ");
        output.append(_graph.get(key));
        output.append("\n");
      }
      return output.toString();
    }
}