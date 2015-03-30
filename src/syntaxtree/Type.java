package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public abstract class Type extends Node{
  private String _name;

  public Type(String name)
  {
    _name = name;
  }

  public Type(String name, int line, int col)
  {
    _name = name;
  }

  public String getName()
  {
    return _name;
  }

  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);
}
