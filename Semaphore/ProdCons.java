import java.util.Scanner;

class Semaphore{
    private volatile int val;

    public Semaphore(int val){
        this.val = val;
    }

    public void waitS(){
        while(val == 0);
        val--;
    }

    public void signalS(){
        val++;
    }
}

class Buffer{
    private int[] buf;
    private int n;

    private int front, rear;

    Semaphore E;
    Semaphore F;
    Semaphore M;
    

    public Buffer(int n){
        this.n = n;
        buf = new int[n];
        front = rear = -1;

        E = new Semaphore(n);
        F = new Semaphore(0);
        M = new Semaphore(1);
    }

    public void produce(int i){
        E.waitS();
        M.waitS();


        rear = (rear+1) % n;
        buf[rear] = i;
        if(front==-1) front = 0;

        System.out.println("Item Produced: "+i);
        this.showBuffer();
        System.out.println();

        F.signalS();
        M.signalS();
    }

    public void consume(){
        F.waitS();
        M.waitS();

        int res = buf[front];
        if(front==rear) front = rear = -1;
        else front = (front+1)%n;

        System.out.println("Item Consumed: " + res);
        this.showBuffer();
        System.out.println();

        E.signalS();
        M.signalS();
    }

    public void showBuffer(){
        System.out.print("Buffer: | ");
        int i;
        for(i=0; i<n; i++){
            if(front<=rear){
                if(i>=front && i<=rear) System.out.print(buf[i]+" | ");
                else System.out.print(" | ");
            }else{
                if(i>rear && i<front) System.out.print(" | ");
                else System.out.print(buf[i]+" | ");
            }
        }

        System.out.println();
    }
}

class Producer extends Thread{
    Buffer buf;
    public Producer(Buffer buff){
        buf = buff;
    }

    public void run(){
        for(int i=1; i<=20; i++){
            buf.produce(i);
            try{
                Thread.sleep(100);
            }catch(Exception e){
                System.out.println("\nProducer Interrupted");
            }
        }
    }
}

class Consumer extends Thread{
    Buffer buf;
    public Consumer(Buffer buff){
        buf = buff;
    }
    public void run(){
        for(int i=1; i<=20; i++){
            buf.consume();
            try{
                Thread.sleep(150);
            }catch(Exception e){
                System.out.println("\nConsumer Interrupted");
            }
        }
    }
}

public class ProdCons{
    public static void main(String[] args){
        Scanner s = new Scanner(System.in);

        System.out.print("Enter Buffer Size: ");
        int n = s.nextInt();
        s.close();

        Buffer buffer = new Buffer(n);
        Producer p = new Producer(buffer);
        Consumer c = new Consumer(buffer);

        p.start();
        c.start();
    }
}