package codegen;

public class CodeGenerator {

  private HashMap<String, String> _registerMap;
  private int _regCount;
  private ArrayList<MIPSInstruction> _mips;
  private ArrayList<IRQuadruple> _irList;
  private int _tempCount;

  public CodeGenerator(ArrayList<IRQuadruple> irList)
  {
    _registerMap = new HashMap<String, String>();
    _regCount = 0;
    _output = new StringBuilder();
    _mips = new ArrayList<MIPSInstruction>();
    _irList = irList;
    _tempCount = 0;
  }

  // walk IR list, generate basic MIPS for each statement

  public void generateCode()
  {
    for(IRQuadruple ir : irList)
    {
      ir.accept(this);
    }
    // link provided library
  }

  public void visit(IRCall n)
  {
    // load args into $a0-3
    // save current regs to stack
    // save return address on the stack
    // jal to label
    return null;
  }

  public void visit(IRAssignment n)
  {

    return null;
  }

  public void visit(IRCopy n)
  {

    return null;
  }

  public void visit(IRParam n)
  {

    return null;
  }

}
