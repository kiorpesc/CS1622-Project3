package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class IdentifierType extends Type {
  public String s;

  public IdentifierType(String as) {
    super(as, 0,0);
    s=as;
  }

  public IdentifierType(String as, int line, int col) {
    super(as, line, col);
    s=as;
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
