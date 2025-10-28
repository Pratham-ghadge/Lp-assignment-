import java.util.Scanner;

class Semaphore {
    private int value;
    public Semaphore(int v) { value = v; }

    // waitS() → decrease value (wait if zero)
    public synchronized void waitS() {
        while (value == 0) {
            try { wait(); } catch (InterruptedException e) {}
        }
        value--;
    }

    // signalS() → increase value (release)
    public synchronized void signalS() {
        value++;
        notify();
    }
}

class Buffer {
    int[] buffer;
    int size, in = 0, out = 0, count = 0;
    Semaphore mutex = new Semaphore(1);   // only one thread at a time
    Semaphore empty, full = new Semaphore(0); // track empty/full slots

    public Buffer(int n) {
        size = n;
        buffer = new int[n];
        empty = new Semaphore(n);
    }

    // Producer adds item
    public void produce(int item) {
        empty.waitS();     // wait if buffer full
        mutex.waitS();     // lock critical section

        buffer[in] = item;
        in = (in + 1) % size;
        count++;
        System.out.println("Produced: " + item + " | Buffer items: " + count);

        mutex.signalS();   // unlock
        full.signalS();    // signal that buffer has item
    }

    // Consumer removes item
    public void consume() {
        full.waitS();      // wait if buffer empty
        mutex.waitS();     // lock critical section

        int item = buffer[out];
        out = (out + 1) % size;
        count--;
        System.out.println("Consumed: " + item + " | Buffer items: " + count);

        mutex.signalS();   // unlock
        empty.signalS();   // signal space is available
    }
}

class Producer extends Thread {
    Buffer b;
    public Producer(Buffer b) { this.b = b; }
    public void run() {
        for (int i = 1; i <= 10; i++) {
            b.produce(i);
            try { Thread.sleep(300); } catch (Exception e) {}
        }
    }
}

class Consumer extends Thread {
    Buffer b;
    public Consumer(Buffer b) { this.b = b; }
    public void run() {
        for (int i = 1; i <= 10; i++) {
            b.consume();
            try { Thread.sleep(500); } catch (Exception e) {}
        }
    }
}

public class ProdCons {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Buffer Size: ");
        int n = sc.nextInt();
        sc.close();

        Buffer b = new Buffer(n);
        Producer p = new Producer(b);
        Consumer c = new Consumer(b);
        p.start();
        c.start();
    }
}
