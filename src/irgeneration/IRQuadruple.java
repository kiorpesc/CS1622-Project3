package irgeneration;

import symboltable.*;
import syntaxtree.*;

// the generic storage format for IR
public class IRQuadruple {

  protected String _op;
  protected String _arg1;
  protected String _arg2;
  protected String _result;

  public IRQuadruple(String op, String arg1, String arg2, String result)
  {
    _op = op;
    _arg1 = arg1;
    _arg2 = arg2;
    _result = result;
  }

  public void setResult(String result)
  {
    _result = result;
  }

  public String toString()
  {
    return "";
  }

}
