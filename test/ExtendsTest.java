class Foo
{
    public static void main(String[] args)
    {
        System.out.println(new Herp().derp(3));
    }
}

class Kerp
{
    int kerpsDerp;

    public int derp(int x)
    {
        return x;
    }
}

class Herp extends Kerp
{
    int herpsDerp;

    public int hueHueHue(int y)
    {
        herpsDerp = 0;
        kerpsDerp = herpsDerp;
        return kerpsDerp;
    }

}
