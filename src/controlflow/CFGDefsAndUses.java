package controlflow;

import symboltable.*;
import irgeneration.*;

import java.util.*;

// computes defs and uses for all blocks in a CFG
public class CFGDefsAndUses
{
    private Map<BasicBlock, Set<SymbolInfo>> _defsPerBlock;
    private Map<BasicBlock, Set<SymbolInfo>> _usesPerBlock;

    public CFGDefsAndUses(ControlFlowGraph graph)
    {
        _defsPerBlock = new HashMap<BasicBlock, Set<SymbolInfo>>();
        _usesPerBlock = new HashMap<BasicBlock, Set<SymbolInfo>>();

        for (BasicBlock b : graph.getAllBlocks())
            computeUsesAndDefs(b);
    }

    // get definitions for a block
    public Set<SymbolInfo> getDefinitions(BasicBlock b)
    {
        return _defsPerBlock.get(b);
    }

    // get usages for a block
    public Set<SymbolInfo> getUsages(BasicBlock b)
    {
        return _usesPerBlock.get(b);
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();

        for (BasicBlock b : _defsPerBlock.keySet())
        {
            result.append("BLOCK\n");
            result.append(b.toString());
            result.append("DEFS:\n");
            for (SymbolInfo sym : getDefinitions(b))
            {
                result.append(sym);
            }
            result.append("USES:\n");
            for (SymbolInfo sym : getUsages(b))
            {
                result.append(sym);
            }
            result.append("END BLOCK\n");
        }

        return result.toString();
    }

    // compute usages and defs for a single block
    private void computeUsesAndDefs(BasicBlock b)
    {
        DefsAndUsesVisitor visitor = new DefsAndUsesVisitor(b.getStatements());
        _defsPerBlock.put(b, visitor.getDefinitions());
        _usesPerBlock.put(b, visitor.getUsages());
    }

    // visits IR statements to determine usages and defs
    private static class DefsAndUsesVisitor implements IRVisitor
    {
        private Set<SymbolInfo> _definitions;
        private Set<SymbolInfo> _usages;

        public DefsAndUsesVisitor(List<IRQuadruple> irList)
        {
            _definitions = new HashSet<SymbolInfo>();
            _usages = new HashSet<SymbolInfo>();

            for (IRQuadruple irq : irList)
                irq.accept(this);
        }

        public Set<SymbolInfo> getDefinitions()
        {
            return _definitions;
        }

        public Set<SymbolInfo> getUsages()
        {
            return _usages;
        }

        private void addUsage(SymbolInfo sym)
        {
            if (!(sym instanceof ConstantSymbol))
            {
                _usages.add(sym);
            }
        }
        private void addDefinition(SymbolInfo sym)
        {
            _definitions.add(sym);
        }

        public void visit(IRArrayAssign n)
        {
            addUsage(n.getArg1());
            addUsage(n.getArg2());
            addUsage(n.getResult());
        }
        public void visit(IRArrayLength n)
        {
            addDefinition(n.getResult());
            addUsage(n.getArg1());
        }
        public void visit(IRArrayLookup n)
        {
            addDefinition(n.getResult());
            addUsage(n.getArg1());
            addUsage(n.getArg2());
        }
        public void visit(IRAssignment n)
        {
            addDefinition(n.getResult());
            addUsage(n.getArg1());
            addUsage(n.getArg2());
        }
        public void visit(IRCall n)
        {
            if (n.getResult() != null)
                addDefinition(n.getResult());
        }
        public void visit(IRCondJump n)
        {
            addUsage(n.getArg1());
        }
        public void visit(IRCopy n)
        {
            addDefinition(n.getResult());
            addUsage(n.getArg1());
        }
        public void visit(IRLabel n)
        {

        }
        public void visit(IRNewArray n)
        {
            addDefinition(n.getResult());
            addUsage(n.getArg2());
        }
        public void visit(IRNewObject n)
        {
            addDefinition(n.getResult());
        }
        public void visit(IRParam n)
        {
            addUsage(n.getArg1());
        }
        public void visit(IRReturn n)
        {
            addUsage(n.getArg1());
        }
        public void visit(IRUnaryAssignment n)
        {
            addDefinition(n.getResult());
            addUsage(n.getArg1());
        }
        public void visit(IRUncondJump n)
        {

        }
    }
}
