package syntaxtree;

public abstract class Node {
  protected int _line;
  protected int _column;

  public Node(int line, int col) {
    _line = line;
    _column = col;
  }

  public int getLine() { return _line; }
  public int getColumn() { return _column; }
}
