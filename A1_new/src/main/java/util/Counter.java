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

    synchronized public void decrement() {
    this.count--;
  }

    public int getCount() {
      return this.count;
    }

    synchronized public void reset() { this.count = 0; }
}
