package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;
import symboltable.SymbolInfo;


public class ArrayLength extends Exp {
  public Exp e;

  public ArrayLength(Exp ae) {
    super(0,0);
    e=ae;
  }

  public ArrayLength(Exp ae, int line, int col) {
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
