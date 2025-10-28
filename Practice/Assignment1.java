
import java.util.*;

class semaphore {

    private int value;

    public semaphore(int v) {
        value = v;
    }

    public synchronized void WaitS() {
        while (value == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        value--;
    }

    public synchronized void signalS() {
        value++;
        notify();
    }

}

class Buffer {

    int[] buffer;
    int size = 0, count = 0, in = 0, out = 0;
    semaphore mutex = new semaphore(1);
    semaphore empty, full = new semaphore(0);

    public Buffer(int n) {
        size = n;
        buffer = new int[n];
        empty = new semaphore(n);

    }

  public void produce(int item) {
        empty.WaitS();
        mutex.WaitS();

        buffer[in] = item;
        in = (in + 1) % size;
        count++;

        System.out.println("Produced" + item + " |  items in Buffer " + count);

        mutex.signalS();
        full.signalS();

    }

    public void Consume() {

        full.WaitS();
        mutex.WaitS();

        int item = buffer[out];

        out = (out + 1) % size;

        count--;

        System.out.println("Consumed" + item + " items in Buffer" + count);
        mutex.signalS();
        empty.signalS();
    }

}

class Producer extends Thread {

    Buffer b;

    public Producer(Buffer b) {
        this.b = b;
    }

    public void run() {

        for (int i = 0; i <= 10; i++) {
            b.produce(i);
            try {
                Thread.sleep(300);
            } catch (Exception e) {
            }
        }
    }

}

class Consumer extends Thread {

    Buffer b;

    public Consumer(Buffer b) {
        this.b = b;
    }

    public void run() {
        for (int i = 0; i <= 10; i++) {
            b.Consume();
            try {
                Thread.sleep(700);
            } catch (Exception e) {
            }
        }
    }
}

public class Assignment1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the Buffer size :");
        int size = sc.nextInt();

        Buffer b = new Buffer(size);
        Producer p = new Producer(b);
        Consumer c = new Consumer(b);
        p.start();
        c.start();

    }

}
