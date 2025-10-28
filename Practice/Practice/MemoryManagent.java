
import java.util.Arrays;
import java.util.Scanner;

public class MemoryManagent {

    static int process[];
    static int blockes[];
    static int allocation[];

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the Number of Blocks:");
        int n = sc.nextInt();
        blockes = new int[n];
        for (int i = 0; i < n; i++) {
            System.out.print("Enter the Size of " + (i + 1) + "Block :");
            blockes[i] = sc.nextInt();
        }

        System.out.print("Enter the Number of Process :");
        n = sc.nextInt();
        process = new int[n];
        for (int i = 0; i < n; i++) {
            System.out.print("Enter the Size of process" + (i + 1));
            process[i] = sc.nextInt();
        }

        boolean exit = true;

        while (exit) {
            System.out.println("Memory Management Statergies:");
            System.out.println("1.First Fit.");
            System.out.println("2.Best Fit.");
            System.out.println("3.Worst Fit.");
            System.out.println("4.Next Fit.");
            System.out.println("5.Exiting...");
            System.out.print("Enter Your Choice :");
            int ch = sc.nextInt();

            switch (ch) {
                case 1:
                    firstFit();
                    break;
                case 2:
                    Bestfit();
                    break;
                case 3:
                    WorstFit();
                    break;
                case 4:
                    nextFit();
                    break;
                case 5:
                    exit = false;
                    System.out.println("............................................");
                    break;

                default:
                    System.out.println("Please Enter the correct Choice !");
                    break;
            }

        }

    }

    static void firstFit() {
        allocation = new int[process.length];
        Arrays.fill(allocation, -1);
        int tempBlock[] = Arrays.copyOf(blockes, blockes.length);
        for (int i = 0; i < process.length; i++) {

            for (int j = 0; j < tempBlock.length; j++) {

                if (process[i] <= tempBlock[j]) {
                    allocation[i] = j;
                    tempBlock[j] -= process[i];
                    break;
                }
            }

        }

        displayResult("First Fit");

    }

    static void Bestfit() {

        allocation = new int[process.length];
        Arrays.fill(allocation, -1);
        int tempBlock[] = Arrays.copyOf(blockes, blockes.length);

        for (int i = 0; i < process.length; i++) {

            int baseindex = -1;

            for (int j = 0; j < tempBlock.length; j++) {

                if (process[i] <= tempBlock[j]) {

                    if (baseindex == -1 || tempBlock[baseindex] > tempBlock[j]) {
                        baseindex = j;

                    }
                }
            }

            if (baseindex != -1) {
                allocation[i] = baseindex;
                tempBlock[baseindex] -= process[i];

            }

            displayResult("best Fit");

        }

    }

    static void WorstFit() {

        allocation = new int[process.length];
        Arrays.fill(allocation, -1);
        int tempBlock[] = Arrays.copyOf(blockes, blockes.length);

        for (int i = 0; i < process.length; i++) {

            int baseindex = -1;

            for (int j = 0; j < tempBlock.length; j++) {

                if (process[i] <= tempBlock[j]) {

                    if (baseindex == -1 || tempBlock[baseindex] < tempBlock[j]) {
                        baseindex = j;

                    }
                }
            }

            if (baseindex != -1) {
                allocation[i] = baseindex;
                tempBlock[baseindex] -= process[i];

            }

            displayResult("best Fit");

        }

    }

    static void nextFit() {

        allocation = new int[process.length];
        Arrays.fill(allocation, -1);
        int tempBlock[] = Arrays.copyOf(blockes, blockes.length);
        int m = tempBlock.length;
        int lastPos = 0;

        for (int i = 0; i < process.length; i++) {

            int j = lastPos;

            int count = 0;

            while (count < m) {
                if (process[i] <= tempBlock[j]) {
                    allocation[i] = j;
                    tempBlock[j] -= process[i];
                    lastPos = (j + 1) % m;

                    break;

                }

                j = (j + 1) % m;
                count++;

            }

        }
        displayResult("next Fit");

    }

    static void displayResult(String method) {
        System.out.println(method + " Memory Management");
        System.out.println("Process \t  Process size \t Block No");

        for (int i = 0; i < process.length; i++) {

            System.out.print((i + 1) + "\t\t" + process[i] + "\t\t");

            if (process[i] == -1) {
                System.out.println("Not allocated");
            } else {
                System.out.println(allocation[i]);
            }

        }

    }
}
