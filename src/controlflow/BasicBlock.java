package controlflow;

import java.util.*;

import irgeneration.*;

public class BasicBlock
{
    private List<IRQuadruple> _statements;

    public BasicBlock()
    {
        _statements = new ArrayList<IRQuadruple>();
    }

    public void addStatement(IRQuadruple q)
    {
        _statements.add(q);
    }

    public boolean isEmpty()
    {
        return _statements.isEmpty();
    }

    public List<IRQuadruple> getStatements()
    {
        return _statements;
    }

    public String toString()
    {
        return toString(0);
    }

    public String toString(int tabLevel)
    {
        StringBuilder result = new StringBuilder();
        addTabs(result, tabLevel);
        result.append("[\n");
        for (IRQuadruple irq : _statements)
        {
            addTabs(result, tabLevel + 1);
            result.append(irq.toString());
            result.append('\n');
        }
        addTabs(result, tabLevel);
        result.append("]\n");
        return result.toString();
    }

    private void addTabs(StringBuilder s, int tabLevel)
    {
        for (int i = 0; i < tabLevel; ++i)
            s.append('\t');
    }
}
