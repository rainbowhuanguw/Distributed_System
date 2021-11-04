package util;

/** Thread-safe Counter */

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
