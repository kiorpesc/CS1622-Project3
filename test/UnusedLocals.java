class Foo
{
    public static void main(String[] args)
    {
        System.out.println(new Derp().derp(1, 2, 3));
    }
}

class Derp
{
    public int derp(int x, int y, int z)
    {
        int a;
        int b;
        int c;
        a = x + 1;
        b = y + 3;
        c = z + 2;
        return b;
    }
}
