CS1622 Project 3b
Charles Kiorpes & Sheridan Zivanovich
crk57@pitt.edu & sdz5@pitt.edu

- BUILDING
    To build on linux, run the following command:
        
        $ make linux 

    To build on Windows, run the following command:
        
        $ make win

    To clean, run the following command:
        
        $ make clean

- RUNNING
    To run our compiler on linux, use the following command:

        $ ./linuxcompile.sh <MiniJava source file>

    To run our compiler on windows, use the following command:

        $ ./compile.sh <MiniJava source file>

    The main class is located in MiniJavaCompiler.java. You can run the program without the shell scripts, but you will need to construct the classpath appropriately (see compile.sh for an example of the classpath). 

NOTES:
    While we were able to continue error checking through parsing and into type checking for the majority of cases, we were unable to fully handle some compounded errors (particularly involving complex parse errors). For example, declaring a method with a parse error in the formal list, as well as an omitted return statement, will cause the parser to crash. 
    
    We included labels in our IR, to be used later if needed.

    Concerning the following error message:
    "Invalid operands for %s operator, at line %d, character %d"
    We felt as though this error message was redundant, considering the only operators available in MiniJava are boolean and integer arithmetic operands, both of which have their own error messages. As such, we did not include this in our output. Attempting to use a class or method name as an operand will result in an error message specific to the operator. 