package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class This extends Exp {

  public This(int line, int col) {
    super(line, col);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
