import java_cup.runtime.*;
%%
%class MiniJavaLexer
%cup
%line
%column

%{
  
  private Symbol createSymbol(int symbol)
  {
    return new Symbol(symbol, yyline + 1, yycolumn + 1);
  }

  private Symbol createSymbol(int symbol, Object value)
  {
    return new Symbol(symbol, yyline + 1, yycolumn + 1, value);
  }

%}

EndOfLine = \n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {EndOfLine} | [' '\t]

BlockComment = "/*" ~"*/"
LineComment = "//" {InputCharacter}* {EndOfLine}?

Identifier = [A-Za-z][A-Za-z_0-9]*

IntegerLiteral = [1-9][0-9]* | 0

%%

<YYINITIAL> {

  "&&"              { return createSymbol(sym.AND); }
  "="               { return createSymbol(sym.ASSIGNMENT); }
  "boolean"         { return createSymbol(sym.BOOLEAN); }
  "class"           { return createSymbol(sym.CLASS); }
  ","               { return createSymbol(sym.COMMA); }
  "."               { return createSymbol(sym.DEREFERENCE); }
  "else"            { return createSymbol(sym.ELSE); }
  "extends"         { return createSymbol(sym.EXTENDS); }
  "false"           { return createSymbol(sym.FALSE); }
  "if"              { return createSymbol(sym.IF); }
  "int"             { return createSymbol(sym.INT); }
  "{"               { return createSymbol(sym.LEFT_BRACE); }
  "["               { return createSymbol(sym.LEFT_BRACKET); }
  "("               { return createSymbol(sym.LEFT_PAREN); }
  "length"          { return createSymbol(sym.LENGTH); }  
  "<"               { return createSymbol(sym.LESS_THAN); }
  "main"            { return createSymbol(sym.MAIN); }
  "-"               { return createSymbol(sym.MINUS); }
  "*"               { return createSymbol(sym.MULTIPLY); }
  "new"             { return createSymbol(sym.NEW); }
  "!"               { return createSymbol(sym.NOT); }
  "+"               { return createSymbol(sym.PLUS); }
  "public"          { return createSymbol(sym.PUBLIC); }
  "return"          { return createSymbol(sym.RETURN); }
  "}"               { return createSymbol(sym.RIGHT_BRACE); }
  "]"               { return createSymbol(sym.RIGHT_BRACKET); }
  ")"               { return createSymbol(sym.RIGHT_PAREN); }
  ";"               { return createSymbol(sym.SEMICOLON); }
  "static"          { return createSymbol(sym.STATIC); }
  "String"          { return createSymbol(sym.STRING); }
  "System.out.println" { return createSymbol(sym.PRINT); }
  "this"            { return createSymbol(sym.THIS); }
  "true"            { return createSymbol(sym.TRUE); }
  "void"            { return createSymbol(sym.VOID); }
  "while"           { return createSymbol(sym.WHILE); }
  {Identifier}      { return createSymbol(sym.IDENTIFIER, yytext()); }
  {IntegerLiteral}  { return createSymbol(sym.INT_LITERAL, Integer.parseInt(yytext())); }


  {BlockComment}    { }
  {LineComment}     { }
  {WhiteSpace}      { }
}

[^]                 { throw new IllegalArgumentException("could not lex " + yytext()); }