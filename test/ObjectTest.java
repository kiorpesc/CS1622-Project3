class Foo
{
    public static void main(String[] args)
    {
        System.out.println(new Derp().what()); // should print 4
    }
}

class Derp
{
    int x;
    int y;

    public int what()
    {
        Derpy d;

        d = new Derpy();
        x = 3;
        System.out.println(this.foo()); // should print 3
        System.out.println(d.nope()); // should print 5
        return x + 1;
    }

    public int foo()
    {
        return x;
    }
}

class Derpy extends Derp
{
    int z;
    int w;
    public int nope()
    {
        x = 5;
        System.out.println(this.bar()); // should print 6
        return x;
    }
    public int bar()
    {
        w = x + 1;
        return w;
    }
}