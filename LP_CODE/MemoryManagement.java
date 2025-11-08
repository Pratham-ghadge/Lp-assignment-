
import java.util.*;

public class MemoryManagement {

    static Scanner sc = new Scanner(System.in);
    static int[] blockSize;
    static int[] processSize;
    static int[] allocation; // stores block index allocated to each process

    public static void main(String[] args) {
        // Input memory blocks
        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        blockSize = new int[m];
        System.out.println("Enter block sizes:");
        for (int i = 0; i < m; i++) {
            System.out.print("Block " + (i + 1) + ": ");
            blockSize[i] = sc.nextInt();
        }

        // Input processes
        System.out.print("\nEnter number of processes: ");
        int n = sc.nextInt();
        processSize = new int[n];
        System.out.println("Enter process sizes:");
        for (int i = 0; i < n; i++) {
            System.out.print("Process " + (i + 1) + ": ");
            processSize[i] = sc.nextInt();
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\n===== MEMORY MANAGEMENT MENU =====");
            System.out.println("1. First Fit");
            System.out.println("2. Best Fit");
            System.out.println("3. Worst Fit");
            System.out.println("4. Next Fit");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> firstFit();
                case 2 -> bestFit();
                case 3 -> worstFit();
                case 4 -> nextFit();
                case 5 -> {
                    exit = true;
                    System.out.println("Exiting...");
                }
                default -> System.out.println("Invalid choice! Try again.");
            }
        }
    }

    // ---------- FIRST FIT ----------
    static void firstFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] temp = Arrays.copyOf(blockSize, blockSize.length);

        for (int i = 0; i < processSize.length; i++) {
            for (int j = 0; j < temp.length; j++) {
                if (temp[j] >= processSize[i]) {
                    allocation[i] = j;
                    temp[j] -= processSize[i];
                    break;
                }
            }
        }
        display("FIRST FIT");
    }

    // ---------- BEST FIT ----------
    static void bestFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] temp = Arrays.copyOf(blockSize, blockSize.length);

        for (int i = 0; i < processSize.length; i++) {
            int best = -1;
            for (int j = 0; j < temp.length; j++) {
                if (temp[j] >= processSize[i]) {
                    if (best == -1 || temp[j] < temp[best])
                        best = j;
                }
            }
            if (best != -1) {
                allocation[i] = best;
                temp[best] -= processSize[i];
            }
        }
        display("BEST FIT");
    }

    // ---------- WORST FIT ----------
    static void worstFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] temp = Arrays.copyOf(blockSize, blockSize.length);

        for (int i = 0; i < processSize.length; i++) {
            int worst = -1;
            for (int j = 0; j < temp.length; j++) {
                if (temp[j] >= processSize[i]) {
                    if (worst == -1 || temp[j] > temp[worst])
                        worst = j;
                }
            }
            if (worst != -1) {
                allocation[i] = worst;
                temp[worst] -= processSize[i];
            }
        }
        display("WORST FIT");
    }

    // ---------- NEXT FIT ----------
    static void nextFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] temp = Arrays.copyOf(blockSize, blockSize.length);

        int last = 0;
        for (int i = 0; i < processSize.length; i++) {
            int count = 0;
            boolean allocated = false;
            int j = last;
            while (count < temp.length) {
                if (temp[j] >= processSize[i]) {
                    allocation[i] = j;
                    temp[j] -= processSize[i];
                    last = (j + 1) % temp.length;
                    allocated = true;
                    break;
                }
                j = (j + 1) % temp.length;
                count++;
            }
        }
        display("NEXT FIT");
    }

    // ---------- DISPLAY RESULT ----------
    static void display(String method) {
        System.out.println("\n===== " + method + " RESULT =====");
        System.out.println("Process\tSize\tBlock");

        for (int i = 0; i < processSize.length; i++) {
            System.out.print("P" + (i + 1) + "\t" + processSize[i] + "\t");
            if (allocation[i] != -1)
                System.out.println("B" + (allocation[i] + 1));
            else
                System.out.println("Not Allocated");
        }

        // Memory block visualization (simple)
        int[] remain = Arrays.copyOf(blockSize, blockSize.length);
        String[] used = new String[blockSize.length];
        Arrays.fill(used, "Empty");

        for (int i = 0; i < processSize.length; i++) {
            int b = allocation[i];
            if (b != -1) {
                remain[b] -= processSize[i];
                if (used[b].equals("Empty"))
                    used[b] = "P" + (i + 1) + "(" + processSize[i] + ")";
                else
                    used[b] += ", P" + (i + 1) + "(" + processSize[i] + ")";
            }
        }

        System.out.println("\n------------------------------------------");
        for (int i = 0; i < blockSize.length; i++) {
            System.out.println("Block " + (i + 1) +
                    " | Total: " + blockSize[i] +
                    " | Used: " + used[i] +
                    " | Rem: " + remain[i]);
        }
        System.out.println("------------------------------------------");
    }
}
