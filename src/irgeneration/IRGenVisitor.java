package irgeneration;

import java.util.ArrayList;

import syntaxtree.*;
import symboltable.*;
import visitor.*;

public class IRGenVisitor {

  private ArrayList<IRQuadruple> _irList;
  private int _tempCount;
  private ISymbolTable _symbolTable;
  private int _ifCount;
  private int _loopCount;
  private int _loopEndCount;

  public IRGenVisitor(ISymbolTable symbols)
  {
    _irList = new ArrayList<IRQuadruple>();
    _tempCount = 0;
    _ifCount = 0;
    _loopCount = 0;
    _loopEndCount = 0;
    _symbolTable = symbols;
  }

  //// UTILITY FUNCTIONS /////

  private VariableSymbol getNextTemp(Type type)
  {
    VariableSymbol nextTemp = new VariableSymbol("_$t" + _tempCount++, type);
    _symbolTable.addVariable(nextTemp);
    return nextTemp;
  }

  // generate a unique method label
  public String generateMethodLabel()
  {
    SymbolInfo currMethod = _symbolTable.getCurrentMethod();
    StringBuilder label = new StringBuilder("_");
    label.append(_symbolTable.getCurrentClass().getName());
    label.append("_");
    label.append(currMethod);
    for(Type t : currMethod.getFormalTypes())
    {
      label.append("_");
      label.append(t.toString());
    }

    return label.toString();
  }

  // generate a unique else label
  public String generateElseLabel()
  {
    String result = generateMethodLabel() + "_ELSE" + _ifCount++;
    return result;
  }

  // generate a unique loop label
  public String generateLoopLabel()
  {
    String result = generateMethodLabel() + "_LOOP" + _loopCount++;
    return result;
  }

  // generate a unique loop end label
  public String generateLoopEndLabel()
  {
    String result = generateMethodLabel() + "_OUT" + _loopEndCount++;
    return result;
  }

  //////// END UTILITY FUNCTIONS ///////////////////


  public SymbolInfo visit(Program n)
  {
    n.m.accept(this);
    for ( int i = 0; i < n.cl.size(); i++ ) {
      n.cl.elementAt(i).accept(this);
    }
    return null;
  }

  public SymbolInfo visit(MainClass n)
  {
    _symbolTable.enterClass(n.i1);
    n.s.accept(this);
    _symboltable.exitClass();
    return null;
  }

  public SymbolInfo visit(ClassDeclSimple n)
  {
    _symbolTable.enterClass(n.i);  // enter the class scope

    for(int i = 0; i < n.ml.size(); i++)  // process all methods
      n.ml.elementAt(i).accept(this);

    _symbolTable.exitClass(); // exit the class scope
    return null;
  }

  public SymbolInfo visit(ClassDeclExtends n)
  {
    _symbolTable.enterClass(n.i);  // enter the class scope

    for(int i = 0; i < n.ml.size(); i++)
      n.ml.elementAt(i).accept(this);

    _symbolTable.exitClass();
    return null;
  }

  //public String visit(VarDecl n){return null;}

  // visit methods and traverse their statements
  public SymbolInfo visit(MethodDecl n)
  {
    _symbolTable.enterMethod(n.i);

    _ifCount = 0; // new method means no ifs encountered yet
    _loopCount = 0; // same for loops
    _loopEndCount = 0; // and their ends


    // create a label for the method
    IRLabel methodLabel = new IRLabel(generateMethodLabel());
    _irList.add(methodLabel);

    // process each statement in the statement_list
    for(int i = 0; i < n.sl.size(); i++)
    {
      n.sl.elementAt(i).accept(this);
    }

    // create return statement IR for the last expression
    SymbolInfo arg1 = n.e.accept(this);
    IRReturn quad = new IRReturn(arg1);
    _irList.add(quad);

    _symbolTable.exitMethod();

    return null;
  }

  //public String visit(Formal n){return null;}
  //public String visit(IntArrayType n){return null;}
  //public String visit(BooleanType n){return null;}
  //public String visit(IntegerType n){return null;}
  //public String visit(IdentifierType n){return null;}

  public SymbolInfo visit(Block n){
    for(int i = 0; i < n.sl.size(); i++)
    {
      n.sl.elementAt(i).accept(this);
    }
    return null;
  }

  public SymbolInfo visit(If n)
  {
    // create first Conditional Jump IR, add to list
    SymbolInfo arg1 = n.e.accept(this);
    String label = generateElseLabel();
    IRCondJump quad = new IRCondJump(arg1, label);
    _irList.add(quad);

    // process the statement in the if clause
    n.s1.accept(this);

    // then add else label to _irList
    IRLabel elseLabel = new IRLabel(label);
    _irList.add(elseLabel);

    // then process the else statement
    n.s2.accept(this);

    return null;
  }

  public SymbolInfo visit(While n)
  {

    // pregenerate loop labels
    String loopStartLabel = generateLoopLabel();
    String loopEndLabel = generateLoopEndLabel();

    // add loop begin label before the check
    IRLabel loopStartIR = new IRLabel(loopStartLabel);
    _irList.add(loopStartIR);

    // conditional jump from while condition
    // iffalse x goto ENDWHILE
    String op = "goto";
    SymbolInfo arg1 = n.e.accept(this);
    IRCondJump quad = new IRCondJump(arg1, loopEndLabel);
    _irList.add(quad);

    // process statements
    n.s.accept(this);

    // goto LOOP START
    IRUncondJump uquad = new IRUncondJump(loopStartLabel);
    _irList.add(uquad);

    // add loop OUT label
    IRLabel loopEndIR = new IRLabel(loopEndLabel);
    _irList.add(loopEndIR);

    return null;
  }

  // TODO: this method - needs to be treated like a Call
  public SymbolInfo visit(Print n)
  {
    // print only needs to take one param?

    // prepare param call
    SymbolInfo arg1 = n.e.accept(this);
    IRParam quad = new IRParam(arg1);
    _irList.add(quad);

    arg1 = new VariableSymbol("System.out.println");  // TODO: this should be a method that is already in the symbol table
    SymbolInfo result = getNextTemp();
    IRCall quadCall = new IRCall(arg1, 1, result);
    _irList.add(quadCall);

    return result;
  }

  public String visit(Assign n)
  {
    String result = n.i.s;
    String arg1 = n.e.accept(this); // generate the other lines of IR first

    IRCopy quad = new IRCopy(arg1, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(ArrayAssign n)
  {
    // TODO: verify how args should be ordered
    SymbolInfo arg1 = n.i.accept(this);
    SymbolInfo arg2 = n.e1.accept(this);
    SymbolInfo result = n.e2.accept(this);
    IRArrayAssign quad = new IRArrayAssign(arg1, arg2, result);
    _irList.add(quad);
    return null;
  }

  public String visit(And n)
  {
    String op = "&&";
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = getNextTemp();
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(LessThan n)
  {
    String op = "<";
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = getNextTemp();
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Plus n)
  {
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = getNextTemp();
    String op = "+";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Minus n)
  {
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = getNextTemp();
    String op = "-";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public String visit(Times n)
  {
    String arg1 = n.e1.accept(this);
    String arg2 = n.e2.accept(this);
    String result = getNextTemp();
    String op = "*";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(ArrayLookup n)
  {
    SymbolInfo arg1 = n.e1.accept(this);
    SymbolInfo arg2 = n.e2.accept(this);
    SymbolInfo result = getNextTemp();
    IRArrayLookup quad = new IRArrayLookup(arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(ArrayLength n)
  {
    SymbolInfo arg1 = n.e.accept(this);
    SymbolInfo result = getNextTemp();
    IRArrayLength quad = new IRArrayLength(arg1, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Call n)
  {
    ArrayList<IRParam> param_list = new ArrayList<IRParam>();

    // create params
    /// IMPLICIT THIS ///
    SymbolInfo arg1 = n.e.accept(this); // get the class to pass in as implicit "this"
    IRParam quad = new IRParam(arg1);
    param_list.add(quad);

    // this loop will ensure that all expressions are evaluated
    // and their IR appended to the irList BEFORE we add
    // the param statements
    for(int i = 0; i < n.el.size(); i++)
    {
      arg1 = n.el.elementAt(i).accept(this);
      quad = new IRParam(arg1);
      param_list.add(quad);
    }

    for(IRParam ir : param_list)
    {
      _irList.add(ir);
    }

    arg1 = n.i.accept(this);
    SymbolInfo result = getNextTemp();
    int nParams = n.el.size() + 1;  // add one for implicit 'this'
    IRCall quadCall = new IRCall(arg1, nParams, result);
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

  // always an int array
  public SymbolInfo visit(NewArray n)
  {
    SymbolInfo size = n.e.accept(this);
    SymbolInfo result = getNextTemp();
    IRNewArray quad = new IRNewArray(size, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(NewObject n)
  {
    SymbolInfo type = n.i.accept(this);
    SymbolInfo result = getNextTemp();
    IRNewObject quad = new IRNewObject(type, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Not n)
  {
    String op = "!";
    SymbolInfo arg1 = n.e.accept(this);
    SymbolInfo result = getNextTemp();
    IRUnaryAssignment quad = new IRUnaryAssignment(op, arg1, result);
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
