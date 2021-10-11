package client1;

/** Thread-safe client1.Counter */

public class Counter {
    private int count;

    public Counter() {
      this.count = 0;
    }

    synchronized public void increment() {
      this.count++;
    }

    public int getVal() {
      return this.count;
    }
}
