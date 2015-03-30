package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class Identifier extends Node{
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
}
