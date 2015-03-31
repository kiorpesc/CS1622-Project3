package irgeneration;

import java.util.ArrayList;

import syntaxtree.*;
import symboltable.*;
import visitor.*;

public class IRGenVisitor {

  private ArrayList<IRQuadruple> _irList;
  private int _tempCount;

  public IRGenVisitor()
  {
    _irList = new ArrayList<IRQuadruple>();
    _tempCount = 0;
  }

  public String visit(Program n)
  {
    n.m.accept(this);
    for ( int i = 0; i < n.cl.size(); i++ ) {
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
    for(int i = 0; i < n.ml.size(); i++)
      n.ml.elementAt(i).accept(this);
    return null;
  }

  public String visit(ClassDeclExtends n)
  {
    for(int i = 0; i < n.ml.size(); i++)
      n.ml.elementAt(i).accept(this);
    return null;
  }

  public String visit(VarDecl n){return null;}

  public String visit(MethodDecl n)
  {
    // TODO: create a label for the method
    for(int i = 0; i < n.sl.size(); i++)
    {
      n.sl.elementAt(i).accept(this);
    }

    // create return statement IR
    String op = "return";
    String arg1 = n.e.accept(this);
    String arg2 = null;
    String result = null;
    IRReturn quad = new IRReturn(op, arg1, arg2, result);

    _irList.add(quad);

    return arg1;
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
    String result = "iffalse";
    String op = "goto";
    String arg1 = n.e.accept(this);
    String arg2 = "ELSELABEL";
    IRCondJump quad = new IRCondJump(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(While n)
  {
    // iffalse x goto ENDWHILE
    String op = "goto";
    String arg2 = "ENDWHILE";
    String arg1 = n.e.accept(this);
    String result = "iffalse";
    IRCondJump quad = new IRCondJump(op, arg1, arg2, result);
    _irList.add(quad);

    // statements
    n.s.accept(this);

    // goto BEGINWHILE
    result = null;
    arg1 = "BEGINWHILE";
    arg2 = null;
    IRUncondJump uquad = new IRUncondJump(op, arg1, arg2, result);
    _irList.add(uquad);

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
    String arg1 = n.e.accept(this); // generate the other lines of IR first
    // grab the last quadruple for modification
    String arg2 = null;
    String op = null;
    IRCopy quad = new IRCopy(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
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
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
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
    _tempCount++;
    IRArrayLength quad = new IRArrayLength(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Call n)
  {
    ArrayList<IRParam> param_list = new ArrayList<IRParam>();

    // create params
    /// IMPLICIT THIS ///
    String op = "param";
    String arg1 = n.e.accept(this); // get the class to pass in as implicit "this"
    String arg2 = null;
    String result = null;
    IRParam quad = new IRParam(op, arg1, arg2, result);
    param_list.add(quad);

    // this loop will ensure that all expressions are evaluated
    // and their IR appended to the irList BEFORE we add
    // the param statements
    for(int i = 0; i < n.el.size(); i++)
    {
      arg1 = n.el.elementAt(i).accept(this);
      quad = new IRParam(op, arg1, arg2, result);
      param_list.add(quad);
    }

    for(IRParam ir : param_list)
    {
      _irList.add(ir);
    }

    op = "call";
    //arg1 = n.e.accept(this);
    arg1 = n.i.accept(this);
    result = "t"+_tempCount++;
    arg2 = ""+(n.el.size() + 1);  // add one for implicit 'this'
    IRCall quadCall = new IRCall(op, arg1, arg2, result);
    _irList.add(quadCall);
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
    _tempCount++;
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

  public String visit(Identifier n)
  {
    return n.s;
  }

  public ArrayList<IRQuadruple> getIRList()
  {
    return _irList;
  }

  public void printIRList()
  {
    System.out.println("Lines: " + _irList.size());
    for(IRQuadruple ir : _irList)
    {
      System.out.println(ir.toString());
    }
  }

}
