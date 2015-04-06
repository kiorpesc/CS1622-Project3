package irgeneration;

import java.util.ArrayList;

import syntaxtree.*;
import symboltable.*;
import visitor.*;

public class IRGenVisitor {

  private ArrayList<IRQuadruple> _irList;
  private int _tempCount;
  private SymbolTable _symbolTable;
  private int _ifCount;
  private int _loopCount;
  private int _loopEndCount;

  public IRGenVisitor(SymbolTable symbols)
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
    MethodSymbol currMethod = _symbolTable.getCurrentMethod();
    StringBuilder label = new StringBuilder("_");
    label.append(_symbolTable.getCurrentClass().getName());
    label.append("_");
    label.append(currMethod.getName());
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
    String result = generateMethodLabel() + "_ELSE" + _ifCount;
    return result;
  }

  public String generateEndIfLabel()
  {
    String result = generateMethodLabel() + "_ENDIF" + _ifCount++;
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

    IRLabel mainLabel = new IRLabel("_main");
    _irList.add(mainLabel);
    _symbolTable.enterClass(n.i1.s);
    n.s.accept(this);
    _symbolTable.exitClass();
    return null;
  }

  public SymbolInfo visit(ClassDeclSimple n)
  {
    _symbolTable.enterClass(n.i.s);  // enter the class scope

    // add implicit 'this'
    VariableSymbol thisLocal = new VariableSymbol("this", new IdentifierType(n.i.s));
    _symbolTable.addVariable(thisLocal);

    for(int i = 0; i < n.ml.size(); i++)  // process all methods
      n.ml.elementAt(i).accept(this);

    _symbolTable.exitClass(); // exit the class scope
    return null;
  }

  public SymbolInfo visit(ClassDeclExtends n)
  {
    _symbolTable.enterClass(n.i.s);  // enter the class scope

    // add implicit 'this'
    VariableSymbol thisLocal = new VariableSymbol("this", new IdentifierType(n.i.s));
    _symbolTable.addVariable(thisLocal);

    for(int i = 0; i < n.ml.size(); i++)
      n.ml.elementAt(i).accept(this);

    _symbolTable.exitClass();
    return null;
  }

  //public String visit(VarDecl n){return null;}

  // visit methods and traverse their statements
  public SymbolInfo visit(MethodDecl n)
  {
    _symbolTable.enterMethod(n.i.s);

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
    String endLabel = generateEndIfLabel();
    IRCondJump quad = new IRCondJump(arg1, label);
    _irList.add(quad);

    // process the statement in the if clause
    n.s1.accept(this);
    IRUncondJump endIf = new IRUncondJump(endLabel);
    _irList.add(endIf);


    // then add else label to _irList
    IRLabel elseLabel = new IRLabel(label);
    _irList.add(elseLabel);

    // then process the else statement
    n.s2.accept(this);

    IRLabel endIR = new IRLabel(endLabel);

    _irList.add(endIR);

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

    MethodSymbol arg = new MethodSymbol("System.out.println", null);  // TODO: this should be a method that is already in the symbol table
    SymbolInfo result = null;
    IRCall quadCall = new IRCall(arg, 1, result);
    _irList.add(quadCall);

    return result;
  }

  public SymbolInfo visit(Assign n)
  {
    SymbolInfo result = n.i.accept(this);
    SymbolInfo arg1 = n.e.accept(this); // generate the other lines of IR first

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

  public SymbolInfo visit(And n)
  {
    String op = "&&";
    SymbolInfo arg1 = n.e1.accept(this);
    SymbolInfo arg2 = n.e2.accept(this);
    SymbolInfo result = getNextTemp(new BooleanType());
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(LessThan n)
  {
    String op = "<";
    SymbolInfo arg1 = n.e1.accept(this);
    SymbolInfo arg2 = n.e2.accept(this);
    SymbolInfo result = getNextTemp(new BooleanType());
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Plus n)
  {
    SymbolInfo arg1 = n.e1.accept(this);
    SymbolInfo arg2 = n.e2.accept(this);
    SymbolInfo result = getNextTemp(new IntegerType());
    String op = "+";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Minus n)
  {
    SymbolInfo arg1 = n.e1.accept(this);
    SymbolInfo arg2 = n.e2.accept(this);
    SymbolInfo result = getNextTemp(new IntegerType());
    String op = "-";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Times n)
  {
    SymbolInfo arg1 = n.e1.accept(this);
    SymbolInfo arg2 = n.e2.accept(this);
    SymbolInfo result = getNextTemp(new IntegerType());
    String op = "*";
    IRAssignment quad = new IRAssignment(op, arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(ArrayLookup n)
  {
    SymbolInfo arg1 = n.e1.accept(this);
    SymbolInfo arg2 = n.e2.accept(this);
    SymbolInfo result = getNextTemp(new IntegerType());
    IRArrayLookup quad = new IRArrayLookup(arg1, arg2, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(ArrayLength n)
  {
    SymbolInfo arg1 = n.e.accept(this);
    SymbolInfo result = getNextTemp(new IntegerType());
    IRArrayLength quad = new IRArrayLength(arg1, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Call n)
  {
    ArrayList<IRParam> param_list = new ArrayList<IRParam>();

    // create params
    /// IMPLICIT THIS ///
    VariableSymbol callClass = (VariableSymbol)n.e.accept(this); // get the class to pass in as implicit "this"
    IRParam quad = new IRParam(callClass);
    param_list.add(quad);

    // this loop will ensure that all expressions are evaluated
    // and their IR appended to the irList BEFORE we add
    // the param statements
    for(int i = 0; i < n.el.size(); i++)
    {
      SymbolInfo arg = n.el.elementAt(i).accept(this);
      quad = new IRParam(arg);
      param_list.add(quad);
    }

    for(IRParam ir : param_list)
    {
      _irList.add(ir);
    }

    IdentifierType classNameType = (IdentifierType)(callClass.getType());
    MethodSymbol name = _symbolTable.getClass(classNameType.s).getMethod(n.i.s);

    SymbolInfo result = getNextTemp(null);
    int nParams = n.el.size() + 1;  // add one for implicit 'this'
    IRCall quadCall = new IRCall(name, nParams, result);
    _irList.add(quadCall);
    return result;
  }

  public SymbolInfo visit(IntegerLiteral n)
  {
    return new ConstantSymbol(""+n.i, new IntegerType());
  }

  public SymbolInfo visit(True n)
  {
      return new ConstantSymbol("true", new BooleanType());
  }

  public SymbolInfo visit(False n)
  {
    return new ConstantSymbol("true", new BooleanType());
  }

  public SymbolInfo visit(IdentifierExp n)
  {
    return _symbolTable.getSymbol(n.s);
  }

  // TODO: is "this" in the symbol table?
  public SymbolInfo visit(This n)
  {
    return _symbolTable.getSymbol("this");
  }

  // always an int array
  public SymbolInfo visit(NewArray n)
  {
    SymbolInfo size = n.e.accept(this);
    SymbolInfo result = getNextTemp(null);
    IRNewArray quad = new IRNewArray(size, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(NewObject n)
  {
    SymbolInfo type = n.i.accept(this);
    SymbolInfo result = getNextTemp(new IdentifierType(n.i.s));
    IRNewObject quad = new IRNewObject(type, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Not n)
  {
    String op = "!";
    SymbolInfo arg1 = n.e.accept(this);
    SymbolInfo result = getNextTemp(new BooleanType());
    IRUnaryAssignment quad = new IRUnaryAssignment(op, arg1, result);
    _irList.add(quad);
    return result;
  }

  public SymbolInfo visit(Identifier n)
  {
    // TODO: what if the symbol is not in the current scope?
    // for example, a method call to an instance of another class
    return _symbolTable.getSymbol(n.s);
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
