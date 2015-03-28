import java.io.*;

import syntaxtree.*;
import visitor.*;

public class PrettyPrinter
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

        new PrettyPrintVisitor().visit(program);
    }
}
