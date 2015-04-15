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
        int i;
        i = 0;
        while (i < 10)
        {
            i = i + 1;
            System.out.println(i);
        }
        return i;
    }
}
