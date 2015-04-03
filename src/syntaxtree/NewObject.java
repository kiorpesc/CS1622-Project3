package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class NewObject extends Exp {
  public Identifier i;

  public NewObject(Identifier ai) {
    super(0,0);
    i=ai;
  }

  public NewObject(Identifier ai, int line, int col) {
    super(line, col);
    i=ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }

  public SymbolInfo accept(IRGenVisitor v) {
    return v.visit(this);
  }
}
