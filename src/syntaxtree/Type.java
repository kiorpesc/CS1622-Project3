package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public abstract class Type extends ASTNode{
  private String _name;

  public Type(String name)
  {
    super(0,0);
    _name = name;
  }

  public Type(String name, int line, int col)
  {
    super(line, col);
    _name = name;
  }

  public String getName()
  {
    return _name;
  }

  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);

  public String toString()
  {
    return _name;
  }
}
