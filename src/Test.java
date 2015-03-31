import java.io.*;

import symboltable.*;
import syntaxtree.*;
import visitor.*;

public class Test
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

        System.out.println("======== Symbol Table ========");
        System.out.println(symbolTable);

                
        System.out.println("======== Errors ========");
        System.out.println(symbolTableBuilder.getErrors());

        if (!symbolTableBuilder.getErrors().isEmpty())
            System.exit(1);

        NameAnalysisVisitor nameAnalysis = new NameAnalysisVisitor(symbolTable);
        nameAnalysis.visit(program);
        System.out.println(nameAnalysis.getErrors());

        if (!nameAnalysis.getErrors().isEmpty())
            System.exit(1);
        
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable);
        typeChecker.visit(program);
        System.out.println(typeChecker.getErrors());
    }
}
