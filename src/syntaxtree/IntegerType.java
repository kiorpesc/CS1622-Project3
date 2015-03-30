package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public class IntegerType extends Type {

  public IntegerType()
  {
    super("int");
  }

  public IntegerType(int line, int col)
  {
    super("int", line, col);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public Type accept(TypeVisitor v) {
    return v.visit(this);
  }
}
