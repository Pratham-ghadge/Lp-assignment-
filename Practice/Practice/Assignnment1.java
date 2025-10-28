import java.util.*;

public class Assignnment1 {
    public static void main(String[] args) {
        System.out.println("Enter the Size of the buffer :");
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        Buffer b = new Buffer(n);
        Producer p = new Producer(b);
        Consumer c = new Consumer(b);
        p.start();
        c.start();
        sc.close();
    }
}

class Semaphore {
    private int value;

    public Semaphore(int v) {
        value = v;
    }

    public synchronized void WaitS() {
        while (value == 0) {
            try {
                wait();

            } catch (Exception e) {
            }
        }
            value--;
        
    }

    public synchronized void SignalS() {

        value++;
        notify();
    }

}

class Buffer {
    int[] buffer;
    int size, in = 0, out = 0, count = 0;
    Semaphore mutex = new Semaphore(1);
    Semaphore empty, full = new Semaphore(0);

    public Buffer(int n) {
        size = n;
        buffer = new int[n];
        empty = new Semaphore(n);
    }

    public void Produce(int item) {
        empty.WaitS();
        mutex.WaitS();

        buffer[in] = item;
        in = (in + 1) % size;
        count++;
        System.out.println("Produced " + item + " | items in buffer :-" + count);
        mutex.SignalS();
        full.SignalS();

    }

    public void Consume() {
        full.WaitS();
        mutex.WaitS();

        int item = buffer[out];

        out = (out + 1) % size;
        count--;
        System.out.println("Consumed " + item + " | items in buffer :-" + count);

        mutex.SignalS();
        empty.SignalS();

    }

}

class Producer extends Thread {

    Buffer b;

    public Producer(Buffer b) {
        this.b = b;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            b.Produce(i);

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
        for (int i = 0; i < 10; i++) {
            b.Consume();

            try {
                Thread.sleep(700);

            } catch (Exception e) {
            }
        }
    }
}