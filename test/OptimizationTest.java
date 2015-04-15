class Opt
{
    public static void main(String[] args)
    {
        System.out.println(new Derp().foo());
    }
}

class Derp
{
    public int foo()
    {
        int x;
        boolean b;

        x = 1;
        b = false && true;

        if (b)
        {
            x = 2;
        }
        else
        {
            x = 3 + 2 + 3 * 2;
        }
        System.out.println(x);

        return x;
    }
}
