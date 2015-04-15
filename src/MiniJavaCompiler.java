import java.io.*;
import java.util.*;

import codegen.*;
import controlflow.*;
import irgeneration.*;
import semanticanalysis.*;
import symboltable.*;
import syntaxtree.*;
import visitor.*;
import regalloc.*;

public class MiniJavaCompiler
{
    public static void main(String[] args) throws Exception
    {
        String outputFileName = "test.asm";
        if (args.length < 2)
        {
            System.err.println("usage: java NameAnalysisTest <input-file> <output-file>");
            System.exit(1);
        }

        MiniJavaParser parser = new MiniJavaParser(new MiniJavaLexer(new FileReader(args[0])));
        outputFileName = args[1];

        Program program = (Program)parser.parse().value;

        BuildSymbolTableVisitor symbolTableBuilder = new BuildSymbolTableVisitor();
        symbolTableBuilder.visit(program);

        ISymbolTable symbolTable = symbolTableBuilder.getSymbolTable();

        for (String error : symbolTableBuilder.getErrors())
            System.out.println(error);

        NameAnalysisVisitor nameAnalysis = new NameAnalysisVisitor(symbolTable);
        nameAnalysis.visit(program);
        for (String error : nameAnalysis.getErrors())
            System.out.println(error);

        TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable);
        typeChecker.visit(program);
        for (String error : typeChecker.getErrors())
            System.out.println(error);

        if(!(typeChecker.getErrors().isEmpty() && nameAnalysis.getErrors().isEmpty() && symbolTableBuilder.getErrors().isEmpty()))
        {
            System.out.println("Errors encountered, cannot generate IR.");
            return;
        }

        IRGenVisitor irGenerator = new IRGenVisitor((SymbolTable)symbolTable);
        irGenerator.visit(program);
        irGenerator.printIRList();

        ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder(irGenerator.getIRList());
        // TODO: register allocation

        for(ControlFlowGraph cfg : cfgBuilder.getControlFlowGraphs())
        {
          LivenessAnalysis la = new LivenessAnalysis(cfg);
          System.out.println(la);

          InterferenceGraph ig = new InterferenceGraph(la, cfg);
          System.out.println(ig);
        }



        CodeGenerator codeGenerator = new CodeGenerator(irGenerator.getIRList());
        codeGenerator.generateCode();
        //codeGenerator.printCode();
        System.out.println("----- OUTPUTTING ASSEMBLY TO: " + outputFileName + " -----");
        codeGenerator.outputMIPSFile(outputFileName); //TODO: make this an argument
    }
}
