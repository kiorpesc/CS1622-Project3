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
        return 3;
    }
}

class Derpy extends Derp
{
    int z;
    int w;
}