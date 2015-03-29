import java.io.*;

import symboltable.*;
import syntaxtree.*;
import visitor.*;

public class NameAnalysisTest
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

        NameAnalysisVisitor nameAnalysis = new NameAnalysisVisitor(symbolTable);
        nameAnalysis.visit(program);
                
        System.out.println("======== Errors ========");
        System.out.println(symbolTableBuilder.getErrors());
        System.out.println(nameAnalysis.getErrors());

    }
}
