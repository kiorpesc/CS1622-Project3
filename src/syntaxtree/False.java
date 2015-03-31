package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class False extends Exp {

  public False(){ super(0,0); }

  public False(int line, int col)
  {
    super(line, col);
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
