package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public abstract class Type {
  private String _name;

  public String getName()
  {
    return _name;
  }

  protected Type(String name)
  {
    _name = name;
  }

  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
