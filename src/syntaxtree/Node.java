package syntaxtree;

public class Node {
  protected int _line;
  protected int _column;

  public Node(int line, int col) {
    _line = line;
    _column = col;
  }

  public int getLine() { return _line; }
  public int getColumn() { return _column; }

  public void setLine(int line)
  {
    _line = line;
  }

  public void setColumn(int column)
  {
    _column = column;
  }
}
