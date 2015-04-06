class Derp
{
    public static void main(String[] args)
    {
        System.out.println(new Kerp().foo());
    }
}

class Kerp
{
    public int foo()
    {
        int x;
        int y;
        int z;

        if (x < 5)
        {
            if (y < 3)
            {
                System.out.println(1);
            }
            else
            {
                System.out.println(2);
            }
        }
        else
        {
            System.out.println(3);
        }
        return 1;
    }
}