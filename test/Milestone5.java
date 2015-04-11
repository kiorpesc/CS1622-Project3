class Foo
{
    public static void main(String[] args)
    {
        System.out.println(new Derp().rec(5));
    }
}

class Derp
{
    public int rec(int x)
    {
        int retVal;

        retVal = 0;
        if (0 < x)
        {
            System.out.println(x);
            retVal = x + this.rec(x - 1);
        }
        else
        {

        }
        return retVal;
    }
}
