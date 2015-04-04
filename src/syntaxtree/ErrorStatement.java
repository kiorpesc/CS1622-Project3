package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;
import irgeneration.IRGenVisitor;
import symboltable.SymbolInfo;

public class ErrorStatement extends Statement
{

  public ErrorStatement(int line, int col)
  {
    super(line, col);
  }

  public void accept(Visitor v) { }
  public Type accept(TypeVisitor v) { return null; }
  public void accept(IRGenVisitor v) { }
}
