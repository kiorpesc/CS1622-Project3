package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public abstract class Exp extends ASTNode{

  public Exp(int line, int col) {
    super(line, col);
  }

  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
  public abstract String accept(IRGenVisitor v);
}
