import java.io.*;
import java.util.*;

public class Pass2{

    private static String buf;
    private static BufferedReader br;
    private static BufferedWriter bw;

    private static HashMap<String, String> symTab = new HashMap<>();
    private static HashMap<String, String> litTab = new HashMap<>();

    private static void initTables() throws IOException{
        br = new BufferedReader(new FileReader("symtab.txt"));
        buf = br.readLine();
        while((buf=br.readLine()) != null){
            String tokens[] = buf.split("\\s+");
            symTab.put(tokens[0], tokens[2]);
        }
        br.close();

        br = new BufferedReader(new FileReader("littab.txt"));
        buf = br.readLine();
        while((buf=br.readLine()) != null){
            String tokens[] = buf.split("\\s+");
            litTab.put(tokens[0], tokens[2]);
        }
        br.close();
    }

    private static void generateMachineCode() throws IOException{
        String tokens[];
        StringBuilder temp;
        int n, t=0;
        br = new BufferedReader(new FileReader("ic.txt"));
        bw = new BufferedWriter(new FileWriter("mc.txt"));

        while((buf=br.readLine()) != null){
            if(buf.equals("")){
                bw.write("\n");
                continue;
            }

            tokens = buf.split(" ");
            n = tokens.length;

            t = 0;

            if(tokens[t].equals("-x-")){
                bw.write("-x-  \n");
                continue;
            }

            bw.write(tokens[t] + "  ");

            temp = new StringBuilder();
            t++;

            if(tokens[t].contains("IS")){
                temp.append("+ ");

                String tk[] = tokens[t].split("[(),]");
                temp.append(tk[2] + " ");
                t++;
                
                if(t==n){ // It was a STOP Instruction
                    temp.append("0 000\n");
                    bw.write(temp.toString());
                    continue;
                }

                if(tokens[t].length() == 3){ // It is an register/conditional statement's code
                    temp.append(tokens[t].charAt(1) + " ");
                    t++;
                }else{
                    temp.append("0 ");
                }

                // Handling Symbols and Literals
                tk = tokens[t].split("[(),]");

                String addr;

                if(tk[1].equals("S")){
                    addr = symTab.get(tk[2]);
                }else{
                    addr = litTab.get(tk[2]);
                }

                temp.append(addr + "\n");
                bw.write(temp.toString());
            }else if(tokens[t].equals("(DL,02)")){
                bw.write("\n");
            }else if(tokens[t].equals("(DL,01)")){
                t++;
                String tk[] = tokens[t].split("[(),]");
                int c = Integer.parseInt(tk[2]);

                temp.append("+ 00 0 " + String.format("%03d", c) + "\n");
                bw.write(temp.toString());
            }else{
                System.out.println("Improper Intermediate Code. Shutting Down!!");
                System.exit(1);
            }   
        }
        br.close();
        bw.close();
    }


    public static void main(String[] args) throws IOException{
        System.out.println("Pass1 Processing...");

        initTables();

        generateMachineCode();

        System.out.println("Pass2 Completed!");
    }
}