class Foo
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
        int[] xs;
        int i;

        xs = new int[20];
        i = 0;

        while (i < 20)
        {
            xs[i] = i;
            i = i + 1;
        }

        System.out.println(this.printArray(xs));
        return 1;
    }

    public int printArray(int[] xs)
    {
        int i;

        i = 0;
        while (i < 20)
        {
            System.out.println(xs[i]);
            i = i + 1;
        }

        return 0;
    }
}