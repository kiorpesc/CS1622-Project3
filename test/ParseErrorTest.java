class Foo
{
    public static void main(String[] args)
    {
        // illegal use of this keyword
        new Bar().bar(class); // bad param list
    }
}

class Bar
{
    public int foo()
    {
        // no return = bad methoddecl
    }

    public int bar(int x)
    {
        int y;;
       
        return x;
    }

    public int baz()
    {
        int x;
        int y;

        // bad param list
        bar(x;x;x);
        x = y = 3; // bad statement

        return 1;
    }

    // bad formal list
    public int quux(int x; int y) 
    {
        int z;;
        return 0;
    }
}

class class Bar // double class = bad classdecl
{

}