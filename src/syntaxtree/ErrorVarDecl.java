package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;

public class ErrorVarDecl extends VarDecl
{

  public ErrorVarDecl(int line, int col) {
    super(null, null, line, col);
  }

  public void accept(Visitor v) {
    return;
  }

  public Type accept(TypeVisitor v) {
    return null;
  }

  public String accept(IRGenVisitor v) {
    return null;
  }
}
