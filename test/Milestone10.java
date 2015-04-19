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
        int i;
        int[] array;

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

        i = 0;
        array = new int[15 + 5];
        while (i < 5)
        {
            array[2 + 2] = 20;
            System.out.println(array[2 * 1 + 2]);
            i = i + 1;
        }

        return x;
    }
}
