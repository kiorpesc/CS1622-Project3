package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class MainClass extends ASTNode{
  public Identifier i1,i2;
  public Statement s;

  public MainClass(Identifier ai1, Identifier ai2, Statement as) {
    super(0,0);
    i1=ai1; i2=ai2; s=as;
  }

  public MainClass(Identifier ai1, Identifier ai2, Statement as, int line, int col) {
    super(line, col);
    i1=ai1; i2=ai2; s=as;
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
