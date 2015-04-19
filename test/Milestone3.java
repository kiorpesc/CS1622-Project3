class Test {
    public static void main(String[] args) {
        System.out.println(new Test2().First(9));
    }
}

class Test2 {

    int z;

    public int First(int y) {
        z = y;
        y = this.Second(y, z, 4);
        z = y + 3;
        return y;
    }

    public int Second(int y, int z, int a) {
      int f;
      f = y + z + a;
      return f;
    }
}
