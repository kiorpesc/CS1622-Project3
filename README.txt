CS1622 Project 3c
Charles Kiorpes & Sheridan Zivanovich
crk57@pitt.edu & sdz5@pitt.edu

BUILDING
    To build on linux, run the following command:

        $ make linux

    To build on Windows, run the following command:

        $ make win

    To clean, run the following command:

        $ make clean

RUNNING
    To run our compiler on linux, use the following command:

        $ ./linuxcompile.sh <MiniJava source file> <destination file name>

    To run our compiler on windows, use the following command:

        $ ./compile.sh <MiniJava source file> <destination file name>

    The main class is located in MiniJavaCompiler.java. You can run the program without the shell scripts, but you will need to construct the classpath appropriately (see compile.sh for an example of the classpath).

    To run with optimizations, add the -O1 flag to the command line arguments to the script. For example, to compile with optimizations on Windows:

        $ ./compile.sh <MiniJava source file> <destination file name> -O1

    See the OPTIMIZATION section for information about the optimization implemented.

MILESTONES:
    We completed the following Milestones:
        Milestone 1
        Milestone 2
        Milestone 3
        Milestone 4
        Milestone 5
        RegAlloc Milestone 1 (Milestone 6)
        Regalloc Milestone 2 (Milestone 7)
        Objects and Arrays   (Milestone 9)
        Optimization         (Milestone 10)

    We did not complete RegAlloc Milestone 3 (Milestone 8). See the files Milestone*.java under the test/ folder for MiniJava source files that test these completed milestones.

OPTIMIZATION:
    We implemented constant folding/constant propagation as an optimization. Constant folding will reduce a statement x := y op z if y and z are both constants. Constant propagation will take a definition x := c, where c is a constant, and replace x with c in a block n if no paths to no go through alternative definitions of x. The definiton x := c is then removed if no remaining usages of x occur between x := c and another definition of x, and x is not an instance variable (we cannot remove assignments to instance variables, since they alter memory). When compiling with optimizations enabled on Milestone10.java, the resulting assembly is reduced by 25 statements. When compiling with optimizations enabled on Milestone7.java, the resulting assembly is reduced by 53 statements.

OTHER TEST FILES:
    We used LinkedList.java (retrieved from the book publisher's website) to test our compiler as well. When executed through MARS, the MIPS produced from our compiler generated the same output as compiling under 'javac' and running the resulting program.

    CoalesceTestSPILL.java tests the clean exiting of our compiler when a spill is detected during register allocation.
