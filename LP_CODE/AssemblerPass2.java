
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class AssemblerPass2 {
    static String buf;
    static BufferedReader br;
    static BufferedWriter bw;

    static HashMap<String, String> symtab = new HashMap<>();
    static HashMap<String, String> littab = new HashMap<>();

    public static void initTables() throws IOException {

        br = new BufferedReader(new FileReader("symtab.txt"));
        buf = br.readLine();
        while ((buf = br.readLine()) != null) {
            String temp[] = buf.split("\\s+");
            symtab.put(temp[0], temp[2]);

        }
        br.close();

        br = new BufferedReader(new FileReader("littab.txt"));
        buf = br.readLine();
        while ((buf = br.readLine()) != null) {
            String temp[] = buf.split("\\s+");
          littab.put(temp[0], temp[2]);

        }
        br.close();

    }

    static void GetMachineCode() throws IOException {

        br = new BufferedReader(new FileReader("ic.txt"));
        bw = new BufferedWriter(new FileWriter("MC.txt"));

        StringBuilder temp ;
        int n, t;

        while ((buf = br.readLine()) != null) {
            if (buf.equals("")) {
                continue;
            }

            String Tokens[] = buf.split("\\s+");
            n = Tokens.length;

            t = 0;
            if (Tokens[t].equals("-x-")) {
                bw.write("-x-\n");
                continue;
            }
            bw.write(Tokens[t] + "\t");
            t++;
            temp = new StringBuilder();

            if (Tokens[t].contains("IS")) {
                temp.append("+");

                String tk[] = Tokens[t].split("[(),]");
                temp.append(tk[2] + "\t");
                t++;

                if (t == n) {
                    temp.append("0\t000\n");
                    bw.write(temp.toString());
                    continue;
                }

                if (Tokens[t].length() == 3) {
                    temp.append(Tokens[t].charAt(1) + "\t");
                    t++;
                } else {
                    temp.append("0\t");
                }

                tk = Tokens[t].split("[(),]");
                String addr;
                if (tk[1].equals("S")) {
                    addr = symtab.get(tk[2]);

                } else {
                    addr = littab.get(tk[2]);
                }

                temp.append(addr + "\n");
                bw.write(temp.toString());

            } else if (Tokens[t].contains("(DL,02)")) {
                bw.write("\n");

            } else if (Tokens[t].contains("(DL,01)")) {
                t++;
                String tk[] = Tokens[t].split("[(),]");
                int c = Integer.parseInt(tk[2]);
                temp.append("+00 0\t" + String.format("%03d", c) + "\n");
                bw.write(temp.toString());
            } else {
                System.out.println("Incorrect Input");
            }

        }
        br.close();
        bw.close();

    }

    public static void main(String[] args) throws IOException {
        System.out.println("Processing start");

        initTables();

        GetMachineCode();

        System.out.println("Processing Complete..");
    }
}
