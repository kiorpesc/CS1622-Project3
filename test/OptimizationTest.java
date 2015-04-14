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

        x = 1;

        if (false)
        {
            x = 2;
        }
        else
        {
            x = 3;
        }

        System.out.println(x);

        return x;
    }
}
