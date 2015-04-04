class MethodCalls{
    public static void main(String[] a){
	System.out.println(new Thing().CallStuff(10));
    }
}

class Thing {

    public int CallStuff(int num){
      int x;

      x = this.CallDepth1(this.CallDepth2());
      return x;
    }

    public int CallDepth1(int y)
    {
      int z;

      z = y + 1;
      return z;
    }

    public int CallDepth2()
    {
      return 3;
    }

}
