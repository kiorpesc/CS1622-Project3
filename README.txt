CS1622 Project 3c
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

        $ ./linuxcompile.sh <MiniJava source file> <destination file name>

    To run our compiler on windows, use the following command:

        $ ./compile.sh <MiniJava source file> <destination file name>

    The main class is located in MiniJavaCompiler.java. You can run the program without the shell scripts, but you will need to construct the classpath appropriately (see compile.sh for an example of the classpath).

    To run with optimizations, add the -O1 flag to the command line arguments to the script. For example, to compile with optimizations on Windows:

        $ ./compile.sh <MiniJava source file> <destination file name> -O1

    See the Notes section for information about the optimization implemented.

NOTES:
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

    We did not complete RegAlloc Milestone 3 (Milestone 8). 