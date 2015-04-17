class Foo
{
    public static void main(String[] args)
    {
        System.out.println(new Derp().what());
    }
}

class Derp
{
    int x;
    int y;

    public int what()
    {
        x = 3;
        System.out.println(this.foo());
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
}