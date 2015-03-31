package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class Formal extends ASTNode{
  public Type t;
  public Identifier i;

  public Formal(Type at, Identifier ai) {
    super(0,0);
    t=at; i=ai;
  }

  public Formal(Type at, Identifier ai, int line, int col) {
    super(line, col);
    t=at; i=ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }

  public String accept(IRGenVisitor v) {
    return v.visit(this);
  }
}
