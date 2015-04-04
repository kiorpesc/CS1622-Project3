package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;
import symboltable.SymbolInfo;

public class ErrorMethodDecl extends MethodDecl
{

  public ErrorMethodDecl(int line, int col) {
    super(null, null, null, null, null, null, line, col);
  }

  public void accept(Visitor v) {
    return;
  }

  public Type accept(TypeVisitor v) {
    return null;
  }

  public void accept(IRGenVisitor v) { }
}
