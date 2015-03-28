JAVAC = javac
JAVA = java
CUP = java -jar lib/java-cup-11a.jar 
JFLEX = java -jar lib/jflex-1.6.0.jar
CLASSPATH = "lib/\*;src/"

all:
	$(CUP) -parser MiniJavaParser src/MiniJavaParser.cup
	mv MiniJavaParser.java src/
	mv sym.java src/
	$(JFLEX) src/MiniJavaLexer.flex
	$(JAVAC) -Xlint:unchecked -cp $(CLASSPATH) src/*.java

clean:
	-rm src/*.class
	-rm src/syntaxtree/*.class
	-rm src/visitor/*.class
	-rm src/symboltable/*.class
	-rm src/MiniJavaLexer.java*
	-rm src/MiniJavaParser.java
	-rm src/sym.java
