package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public abstract class Exp extends ASTNode{

  public Exp(int line, int col) {
    super(line, col);
  }

  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
  public abstract String accept(IRGenVisitor v);
}
