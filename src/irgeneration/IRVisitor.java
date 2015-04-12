package irgeneration;

public interface IRVisitor
{
    public void visit(IRArrayAssign n);
    public void visit(IRArrayLength n);
    public void visit(IRArrayLookup n);
    public void visit(IRAssignment n);
    public void visit(IRCall n);
    public void visit(IRCondJump n);
    public void visit(IRCopy n);
    public void visit(IRLabel n);
    public void visit(IRNewArray n);
    public void visit(IRNewObject n);
    public void visit(IRParam n);
    public void visit(IRReturn n);
    public void visit(IRUnaryAssignment n);
    public void visit(IRUncondJump n);
}