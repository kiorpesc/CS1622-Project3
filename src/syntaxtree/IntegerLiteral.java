package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;
import symboltable.SymbolInfo;

public class IntegerLiteral extends Exp {
  public int i;

  public IntegerLiteral(int ai) {
    super(0,0);
    i=ai;
  }

  public IntegerLiteral(int ai, int line, int col) {
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
