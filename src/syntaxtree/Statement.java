package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public abstract class Statement extends Node{

  public Statement(int line, int col)
  {
      super(line, col);
  }

  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
