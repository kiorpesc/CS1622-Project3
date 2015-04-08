JAVAC = javac
JAVA = java
CUP = java -jar lib/java-cup-11a.jar 
JFLEX = java -jar lib/jflex-1.6.0.jar
CLASSPATH_WIN = "lib/\*;src/"
CLASSPATH_LINUX = "lib/java-cup-11a.jar:src/"

win:
	$(CUP) -parser MiniJavaParser src/MiniJavaParser.cup
	mv MiniJavaParser.java src/
	mv sym.java src/
	$(JFLEX) src/MiniJavaLexer.flex
	$(JAVAC) -cp $(CLASSPATH_WIN) src/*.java

linux:
	$(CUP) -parser MiniJavaParser src/MiniJavaParser.cup
	mv MiniJavaParser.java src/
	mv sym.java src/
	$(JFLEX) src/MiniJavaLexer.flex
	$(JAVAC) -cp $(CLASSPATH_LINUX) src/*.java

clean:
	-rm src/*.class
	-rm src/syntaxtree/*.class
	-rm src/visitor/*.class
	-rm src/symboltable/*.class
	-rm src/semanticanalysis/*.class
	-rm src/irgeneration/*.class
	-rm src/codegen/*.class
	-rm src/MiniJavaLexer.java*
	-rm src/MiniJavaParser.java
	-rm src/sym.java
	-rm *.asm
