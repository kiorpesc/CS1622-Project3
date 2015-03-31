package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class NewArray extends Exp {
  public Exp e;

  public NewArray(Exp ae) {
    super(0,0);
    e=ae;
  }

  public NewArray(Exp ae, int line, int col) {
    super(line, col);
    e=ae;
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
