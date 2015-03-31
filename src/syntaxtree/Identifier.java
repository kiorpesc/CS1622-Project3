package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class Identifier extends ASTNode{
  public String s;

  public Identifier(String as) {
    super(0,0);
    s=as;
  }

  public Identifier(String as, int line, int col) {
    super(line, col);
    s=as;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }

  public String toString(){
    return s;
  }

  public String accept(IRGenVisitor v) {
    return v.visit(this);
  }
}
