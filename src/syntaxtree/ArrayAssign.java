package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class ArrayAssign extends Statement {
  public Identifier i;
  public Exp e1,e2;

  public ArrayAssign(Identifier ai, Exp ae1, Exp ae2) {
    super(0,0);
    i=ai; e1=ae1; e2=ae2;
  }

  public ArrayAssign(Identifier ai, Exp ae1, Exp ae2, int line, int col) {
    super(line, col);
    i=ai; e1=ae1; e2=ae2;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }

  public void accept(IRGenVisitor v) {
    v.visit(this);
  }
}
