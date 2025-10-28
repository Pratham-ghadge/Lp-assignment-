import java.util.*;

public class MemoryManagement {

    static Scanner sc = new Scanner(System.in);
    static int[] blockSize;
    static int[] processSize;
    static int[] allocation; // stores block index allocated to each process

    public static void main(String[] args) {
        // Input blocks
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
                case 1: firstFit(); break;
                case 2: bestFit(); break;
                case 3: worstFit(); break;
                case 4: nextFit(); break;
                case 5: exit = true; System.out.println("Exiting..."); break;
                default: System.out.println("Invalid choice! Try again.");
            }
        }
    }

    // ---------- FIRST FIT ----------
    static void firstFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] tempBlock = Arrays.copyOf(blockSize, blockSize.length);

        for (int i = 0; i < processSize.length; i++) {
            for (int j = 0; j < tempBlock.length; j++) {
                if (tempBlock[j] >= processSize[i]) {
                    allocation[i] = j;
                    tempBlock[j] -= processSize[i];
                    break;
                }
            }
        }
        displayResult("FIRST FIT");
    }

    // ---------- BEST FIT ----------
    static void bestFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] tempBlock = Arrays.copyOf(blockSize, blockSize.length);

        for (int i = 0; i < processSize.length; i++) {
            int bestIdx = -1;
            for (int j = 0; j < tempBlock.length; j++) {
                if (tempBlock[j] >= processSize[i]) {
                    if (bestIdx == -1 || tempBlock[j] < tempBlock[bestIdx])
                        bestIdx = j;
                }
            }
            if (bestIdx != -1) {
                allocation[i] = bestIdx;
                tempBlock[bestIdx] -= processSize[i];
            }
        }
        displayResult("BEST FIT");
    }

    // ---------- WORST FIT ----------
    static void worstFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] tempBlock = Arrays.copyOf(blockSize, blockSize.length);

        for (int i = 0; i < processSize.length; i++) {
            int worstIdx = -1;
            for (int j = 0; j < tempBlock.length; j++) {
                if (tempBlock[j] >= processSize[i]) {
                    if (worstIdx == -1 || tempBlock[j] > tempBlock[worstIdx])
                        worstIdx = j;
                }
            }
            if (worstIdx != -1) {
                allocation[i] = worstIdx;
                tempBlock[worstIdx] -= processSize[i];
            }
        }
        displayResult("WORST FIT");
    }

    // ---------- NEXT FIT ----------
    static void nextFit() {
        allocation = new int[processSize.length];
        Arrays.fill(allocation, -1);
        int[] tempBlock = Arrays.copyOf(blockSize, blockSize.length);

        int lastPos = 0; // starting block for search
        int m = tempBlock.length;

        for (int i = 0; i < processSize.length; i++) {
            int j = lastPos;
            boolean allocated = false;
            int count = 0; // to prevent infinite loop

            while (count < m) {
                if (tempBlock[j] >= processSize[i]) {
                    allocation[i] = j;
                    tempBlock[j] -= processSize[i];
                    lastPos = (j + 1) % m; // next search starts from next block
                    allocated = true;
                    break;
                }
                j = (j + 1) % m;
                count++;
            }
        }
        displayResult("NEXT FIT");
    }

    // ---------- DISPLAY RESULT ----------
    static void displayResult(String method) {
        System.out.println("\n===== " + method + " ALLOCATION RESULT =====");
        System.out.println("Process No.\tProcess Size\tBlock No.");
        for (int i = 0; i < processSize.length; i++) {
            System.out.print((i + 1) + "\t\t" + processSize[i] + "\t\t");
            if (allocation[i] != -1)
                System.out.println((allocation[i] + 1));
            else
                System.out.println("Not Allocated");
        }
    }
}