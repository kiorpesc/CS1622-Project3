import java.io.*;
import java.util.*;

import codegen.*;
import irgeneration.*;
import semanticanalysis.*;
import symboltable.*;
import syntaxtree.*;
import visitor.*;

public class MiniJavaCompiler
{
    public static void main(String[] args) throws Exception
    {
        if (args.length < 1)
        {
            System.err.println("usage: java NameAnalysisTest <input-file>");
            System.exit(1);
        }

        MiniJavaParser parser = new MiniJavaParser(new MiniJavaLexer(new FileReader(args[0])));

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

        CodeGenerator codeGenerator = new CodeGenerator(irGenerator.getIRList());
        codeGenerator.generateCode();
        codeGenerator.printCode();
    }
}
