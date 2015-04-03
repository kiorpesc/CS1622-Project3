package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class True extends Exp {

  public True(){ super(0,0); }

  public True(int line, int col)
  {
    super(line, col);
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
