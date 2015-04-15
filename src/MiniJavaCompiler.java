import java.io.*;
import java.util.*;

import codegen.*;
import controlflow.*;
import irgeneration.*;
import optimization.*;
import semanticanalysis.*;
import symboltable.*;
import syntaxtree.*;
import visitor.*;

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

        List<IRQuadruple> irList = irGenerator.getIRList();
        ControlFlowGraphBuilder cfgBuilder = new ControlFlowGraphBuilder(irList);

        // optimization
        // TODO: control this with command line flag
        ConstantFolder folder = new ConstantFolder(irList);
        ConstantPropagator prop = new ConstantPropagator(irList, cfgBuilder.getControlFlowGraphs());
        DeadCodeEliminator elim = new DeadCodeEliminator(cfgBuilder.getControlFlowGraphs(), irList);

        while (folder.wasOptimized() || prop.wasOptimized() || elim.wasOptimized())
        {
            System.out.println("repeating optimization");
            cfgBuilder = new ControlFlowGraphBuilder(irList);
            prop = new ConstantPropagator(irList, cfgBuilder.getControlFlowGraphs());
            folder = new ConstantFolder(irList);
            elim = new DeadCodeEliminator(cfgBuilder.getControlFlowGraphs(), irList);
        }

        System.out.println("----- OPTIMIZED IR -----");
        for (IRQuadruple irq : irList)
        {
            System.out.println(irq);
        }

        // TODO: register allocation
        CodeGenerator codeGenerator = new CodeGenerator(irList);
        codeGenerator.generateCode();
        //codeGenerator.printCode();
        System.out.println("----- OUTPUTTING ASSEMBLY TO: " + outputFileName + " -----");
        codeGenerator.outputMIPSFile(outputFileName); //TODO: make this an argument
    }
}
