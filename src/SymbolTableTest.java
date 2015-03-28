import java.io.*;

import symboltable.*;
import syntaxtree.*;
import visitor.*;

public class SymbolTableTest
{
    public static void main(String[] args) throws Exception
    {
        if (args.length < 1)
        {
            System.err.println("usage: java PrettyPrinter <input-file>");
            System.exit(1);
        }

        MiniJavaParser parser = new MiniJavaParser(new MiniJavaLexer(new FileReader(args[0])));

        Program program = (Program)parser.parse().value;

        BuildSymbolTableVisitor v = new BuildSymbolTableVisitor();
        v.visit(program);

        System.out.println(v.getSymbolTable());
        System.out.println(v.getErrors());
    }
}
