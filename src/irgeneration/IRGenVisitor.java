package irgeneration;

import visitor.*;

public class IRGenVisitor {

  private ArrayList<IRQuadruple> _irList;
  private int _tempCount;

  public IRGenVisitor()
  {
    _irList = new ArrayList<IRQuadruple>();
    _tempCount = 0;
  }

  public void visit(Program n)
  {
    n.m.accept(this);
    for ( int i = 0; i < n.cl.size(); i++ ) {
      System.out.println();
      n.cl.elementAt(i).accept(this);
    }
    return null;
  }

  public String visit(MainClass n)
  {
    return n.s.accept(this);
  }

  public String visit(ClassDeclSimple n)
  {
    return n.aml.accept(this);
  }

  public String visit(ClassDeclExtends n)
  {
    return n.aml.accept(this);
  }

  public String visit(VarDecl n){return null;}

  public String visit(MethodDecl n)
  {
    // TODO: create a label for the method
    for(int i = 0; i < n.sl.size(); i++)
    {
      n.sl.elementAt(i).accept(this);
    }
    return null;
  }

  public String visit(Formal n){return null;}
  public String visit(IntArrayType n){return null;}
  public String visit(BooleanType n){return null;}
  public String visit(IntegerType n){return null;}
  public String visit(IdentifierType n){return null;}

  public String visit(Block n){
    for(int i = 0; i < n.sl.size(); i++)
    {
      n.sl.elementAt(i).accept(this);
    }
    return null;
  }

  public String visit(If n)
  {
    return null;
  }

  public String visit(While n)
  {
    return null;
  }

  // TODO: this method
  public String visit(Print n)
  {
    return null;
  }

  public String visit(Assign n)
  {
    String result = n.i.s;
    n.e.accept(this); // generate the other lines of IR first
    // grab the last quadruple for modification
    IRQuaddruple quad = (IRQuadruple)_irList.remove(_irList.size()-1);
    quad.setResult(result);
    _irList.add(quad);
    return null;
  }

  public String visit(ArrayAssign n)
  {
    // fuck?
    return null;
  }

  public String visit(And n)
  {
    String op = "&&";
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = "t"+_tempCount++;
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(LessThan n)
  {
    String op = "<";
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = "t"+_tempCount++;
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Plus n)
  {
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = "t"+_tempCount;
    _tempCount++;
    String op = "+";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Minus n)
  {
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = "t"+_tempCount;
    _tempCount++;
    String op = "-";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Times n)
  {
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = "t"+_tempCount;
    _tempCount++;
    String op = "*";
    Assignment quad = new Assignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(ArrayLookup n)
  {
    String arg1 = n.e1.accept(this);
    String arg2 = n.e1.accept(this);
    String result = "t"+_tempCount;
    _tempCount++;
    String op = "";
    IRArrayLookup quad = new IRArrayLookup(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(ArrayLength n)
  {
    String op = "length";
    String arg1 = n.e.accept(this);
    String arg2 = "";
    String result = "t"+_tempCount;
    tempCount++;
    IRArrayLength quad = new IRArrayLength(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Call n)
  {
    String op = "call";
    String arg1 = n.e.accept(this);
    String arg1 += n.i;
    String result = "t"+_tempCount;
    tempCount++;
    String arg2 = ""+n.el.size();
    IRCall quad = new IRCall(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(IntegerLiteral n)
  {
    return "" + n.i;
  }

  public String visit(True n)
  {
      return "true";
  }

  public String visit(False n)
  {
    return "false";
  }

  public String visit(IdentifierExp n)
  {
    return n.s;
  }

  public String visit(This n)
  {
    return "this";
  }

  public String visit(NewArray n)
  {
    String op = "new";
    String arg1 = "int";
    String arg2 = n.e.accept(this);
    String result = "t"+_tempCount;
    _tempCount++;
    IRNewArray quad = new IRNewArray(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(NewObject n)
  {
    String op = "new";
    String arg1 = n.i.accept(this);
    String arg2 = null;
    String result = "t"+_tempCount;
    tempCount++;
    IRNewObject quad = new IRNewObject(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Not n)
  {
    String op = "!";
    String arg1 = n.e.accept(this);
    String arg2 = null;
    String result = "t"+_tempCount++;
    IRUnaryAssignment quad = new IRUnaryAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public void visit(Identifier n)
  {
    return n.s;
  }

  public ArrayList<IRQuadruple> getIRList()
  {
    return _irList;
  }

  public void printIRList()
  {
    for(IRQuadruple ir : _irList)
    {
      System.out.println(ir.toString());
    }
  }

}
