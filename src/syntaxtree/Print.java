package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class Print extends Statement {
  public Exp e;

  public Print(Exp ae) {
    super(0,0);
    e=ae;
  }

  public Print(Exp ae, int line, int col) {
    super(line, col);
    e=ae;
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
