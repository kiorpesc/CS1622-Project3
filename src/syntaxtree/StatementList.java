package syntaxtree;

import java.util.Vector;

public class StatementList extends Node{
   private Vector list;

   public StatementList() {
      list = new Vector();
   }

   public StatementList(int line, int col) {
      super(line, col);
      list = new Vector();
   }

   public void addElement(Statement n) {
      list.addElement(n);
   }

   public void addFront(Statement n) {
      list.add(0, n);
   }

   public Statement elementAt(int i)  {
      return (Statement)list.elementAt(i);
   }

   public int size() {
      return list.size();
   }
}
