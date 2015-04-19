class Foo
{
    public static void main(String[] args)
    {
        System.out.println(new Derp().foo()); // shuold print 32
    }
}

class Derp
{
    int x;
    Derpy d;
    int[] arr;
    boolean b;

    public int foo()
    {
        int i;

        i = 0;
        d = new Derpy();
        x = 32;
        b = true;
        arr = new int[32];

        while (i < 32)
        {
            arr[i] = i;
            i = i + 1;
        }

        return this.bar();

    }

    public int bar()
    {
        int i;

        if (b)
        {
            System.out.println(1); // should print 1
        }
        else
        {
            System.out.println(0); // should not print
        }

        System.out.println(d.derp()); // should print 5
        System.out.println(d.derpy()); // should print 5

        i = 0;
        while (i < 32) // shuold print 0..31
        {
            System.out.println(arr[i]);
            i = i + 1;
        }

        return x;
    }

}

class Derpy
{
    int a;
    public int derp()
    {
        a = 5;
        return a;
    }

    public int derpy()
    {
        return a;
    }
}

