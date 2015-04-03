class Foo
{
    public static void main(String[] args)
    {
        // illegal use of this keyword
        Foo = this;
        return foo;
    }
}

class Bar
{
    public int foo(;)
    {
        return 1;
    }
    public int foo()
    {
        int x;
        boolean f;

        f = true;

        // bad l-value
        Bar = 3;

        // invalid operands
        x = Bar + Bar; 

        // non-method call
        x = this.x(); 

        // wrong type
        x = this.bar(new Bar());

        // wrong number of arguments
        x = this.bar();

        // non-integer operands
        f = f + f;

        // attempt to use boolean operator
        x = x && x;

        // length on non-array
        x = x.length;

        // non boolean expression
        if (x)
        {
            System.out.println(x);
        }
        else
        {
            System.out.println(x);
        }

        // non boolean expression
        while (x)
        {
            System.out.println(x);
        }

        // type mismatch
        x = true;

        // unidentified lvalue + invalid rvalue
        y = bar;

        // TODO: inheritance types*/
        return 3;
    }

    public int bar(int x)
    {
        return x;
    }
}
