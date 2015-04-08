class Test {
    public static void main(String[] args) {
        System.out.println(new Test2().First(9));
    }
}

class Test2 {
    public int First(int y) {
        y = this.Second(y, 3, 4);
        return y;
    }

    public int Second(int y, int z, int a) {
      int f;
      f = y + z + a;
      return f;
    }
}
