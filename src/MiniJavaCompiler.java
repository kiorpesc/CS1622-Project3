import java.io.*;
import java.util.*;

import codegen.*;
import controlflow.*;
import irgeneration.*;
import objectimpl.*;
import optimization.*;
import semanticanalysis.*;
import symboltable.*;
import syntaxtree.*;
import visitor.*;
import regalloc.*;

public class MiniJavaCompiler
{
    private static boolean _optimize = false;

    public static void main(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            System.err.println("usage: java NameAnalysisTest <input-file> <output-file>");
            System.exit(1);
        }

        args = parseFlags(args);

        // parse the program
        Program program = parseProgram(args[0]);

        // build symbol table and check for semantic errors
        List<ErrorChecker> errorCheckers = new ArrayList<ErrorChecker>();
        ISymbolTable symbolTable = buildSymbolTableAndCheckErrors(program, errorCheckers);

        // loop over error checkers, printing out each error
        boolean failed = false;
        for (ErrorChecker checker : errorCheckers)
        {
            if (checker.hasErrors())
            {
                failed = true;
                for (String error : checker.getErrors())
                {
                    System.out.println(error);
                }
            }

        }
        // exit if errors are encountered.
        if (failed)
        {
            System.out.println("Errors encountered, cannot generate IR.");
            return;
        }

        // generate IR (3-address code)
        IRGenVisitor irGenerator = new IRGenVisitor((SymbolTable)symbolTable);
        irGenerator.visit(program);
        irGenerator.printIRList();

        // determine class variable offsets
        ObjectLayoutManager objLayoutMgr = new ObjectLayoutManager(symbolTable.getClasses());

        // optimizations!
        List<IRQuadruple> irList = irGenerator.getIRList();
        if (_optimize)
        {
            irList = runOptimizations(irList, objLayoutMgr);
            System.out.println("----- OPTIMIZED IR -----");
            for (IRQuadruple irq : irList)
            {
                System.out.println(irq);
            }
        }

        ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder(irList);

        Map<SymbolInfo, Integer> regColors = new HashMap<SymbolInfo, Integer>();

        for(ControlFlowGraph cfg : cfgBuilder.getControlFlowGraphs())
        {
          LivenessAnalysis la = new LivenessAnalysis(cfg);
          System.out.println(la);

          InterferenceGraph ig = new InterferenceGraph(la, cfg);
          //System.out.println(ig);

          RegisterAllocator regAlloc = new RegisterAllocator(ig, 22);
          System.out.println(regAlloc);
          regColors.putAll(regAlloc.getColors());
        }

        CodeGenerator codeGenerator = new CodeGenerator(irList, regColors, objLayoutMgr);
        codeGenerator.generateCode();
        //codeGenerator.printCode();
        String outputFileName = args[1];
        System.out.println("----- OUTPUTTING ASSEMBLY TO: " + outputFileName + " -----");
        codeGenerator.outputMIPSFile(outputFileName); //TODO: make this an argument
    }

    private static String[] parseFlags(String[] args)
    {
        List<String> argsList = new ArrayList<String>(Arrays.asList(args));

        if (argsList.contains("-O1"))
        {
            _optimize = true;
            argsList.remove("-O1");
        }
        return argsList.toArray(new String[0]);
    }

    private static List<IRQuadruple> runOptimizations(List<IRQuadruple> irList, ObjectLayoutManager objLayoutMgr)
    {
        IROptimizer optimizer = new IROptimizer(irList, objLayoutMgr);
        optimizer.optimize();
        return optimizer.getOptimizedIR();
    }

    private static Program parseProgram(String sourceFileName) throws Exception
    {
        MiniJavaParser parser = new MiniJavaParser(new MiniJavaLexer(new FileReader(sourceFileName)));
        return (Program)parser.parse().value;
    }

    private static ISymbolTable buildSymbolTableAndCheckErrors(Program program, List<ErrorChecker> errorCheckers)
    {
        // build symbol table
        BuildSymbolTableVisitor symbolTableBuilder = new BuildSymbolTableVisitor();
        symbolTableBuilder.visit(program);

        ISymbolTable symbolTable = symbolTableBuilder.getSymbolTable();

        // do name analysis (undefined references)
        NameAnalysisVisitor nameAnalysis = new NameAnalysisVisitor(symbolTable);
        nameAnalysis.visit(program);

        // do type checking
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable);
        typeChecker.visit(program);

        // aggregate our error checkers
        errorCheckers.add(symbolTableBuilder);
        errorCheckers.add(nameAnalysis);
        errorCheckers.add(typeChecker);

        return symbolTable;
    }

}
