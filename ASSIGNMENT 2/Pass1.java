import java.io.*;
import java.util.*;

class Symbol{
    String sym;
    int addr;
}

class Literal{
    String lit;
    int addr;
}

public class Pass1{
    private static HashMap<String, Integer> opcodeTable = new HashMap<>();
    private static HashMap<String, Integer> regTable = new HashMap<>();
    private static HashMap<String, Integer> condTable = new HashMap<>();
    private static HashMap<String, Integer> adTable = new HashMap<>();
    private static HashMap<String, Integer> dlTable = new HashMap<>();

    private static int MAX = 20;
    private static String buf;
    private static int lc, litcnt = 0, poolcnt = 0, proclit = 0, symcount = 0;

    private static Symbol SYMTAB[] = new Symbol[MAX];
    private static Literal LITTAB[] = new Literal[MAX];
    private static int POOLTAB[] = new int[MAX];


    private static void init(){
        opcodeTable.put("STOP", 0);
        opcodeTable.put("ADD", 1);
        opcodeTable.put("SUB", 2);
        opcodeTable.put("MULT", 3);
        opcodeTable.put("MOVER", 4);
        opcodeTable.put("MOVEM", 5);
        opcodeTable.put("COMP", 6);
        opcodeTable.put("BC", 7);
        opcodeTable.put("DIV", 8);
        opcodeTable.put("READ", 9);
        opcodeTable.put("PRINT", 10);

        regTable.put("AREG", 1);
        regTable.put("BREG", 2);
        regTable.put("CREG", 3);
        regTable.put("DREG", 4);

        condTable.put("LT", 1);
        condTable.put("LE", 2);
        condTable.put("EQ", 3);
        condTable.put("GT", 4);
        condTable.put("GE", 5);
        condTable.put("ANY", 6);

        adTable.put("START", 1);
        adTable.put("END", 2);
        adTable.put("ORIGIN", 3);
        adTable.put("EQU", 4);
        adTable.put("LTORG", 5);

        dlTable.put("DC", 1);
        dlTable.put("DS", 2);

        //assign memory in advance for symbols 
    	for(int i=0;i<MAX;i++){
    		SYMTAB[i]=new Symbol();
    	}
    	

    	//assign memory in advance for literals
    	for(int i=0;i<MAX;i++){
    		LITTAB[i]=new Literal();
    	}
    }

    private static int searchOpcodeTable(String s){
        if(opcodeTable.containsKey(s)) return opcodeTable.get(s);
        return -1;
    }

    private static int searchRegTable(String s){
        if(regTable.containsKey(s)) return regTable.get(s);
        return -1;
    }

    private static int searchCondTable(String s){
        if(condTable.containsKey(s)) return condTable.get(s);
        return -1;
    }

    private static int searchAdTable(String s){
        if(adTable.containsKey(s)) return adTable.get(s);
        return -1;
    }

    private static int searchDlTable(String s){
        if(dlTable.containsKey(s)) return dlTable.get(s);
        return -1;
    }

    private static int searchSymbolTable(String s){
        for(int i=0; i<symcount; i++){
            if(SYMTAB[i].sym.equals(s)) return i;
        }
        return -1;
    }

    private static boolean isLabel(String s){
        return (searchAdTable(s) == -1 && searchDlTable(s) == -1 && searchOpcodeTable(s) == -1);
    }

    private static void errorHandler(int cs){
        System.out.println("Error Occured Somewhere at case " + cs);
        System.exit(1);
    }

    private static void generateIC() throws IOException{
        int n, i = 0, j=0, k=0;

        BufferedReader fs = new BufferedReader(new FileReader("source.txt"));

        BufferedWriter fd = new BufferedWriter(new FileWriter("ic.txt"));

        while((buf = fs.readLine()) != null){
            String[] tokens = buf.split(" |\\,");
            n = tokens.length;
            int t = 0;
            String curLabel = "";
            
            if(tokens.length == 0) continue;
            String currToken = tokens[t];

            if(isLabel(currToken)){
                i = searchSymbolTable(currToken);
                curLabel = currToken;

                if(i==-1){
                    SYMTAB[symcount].sym = currToken;
                    SYMTAB[symcount].addr = lc;
                    symcount++;
                }else{
                    SYMTAB[i].addr = lc;
                }

                if(t<n){
                    t++;
                }else{
                    errorHandler(1);
                }
            }

            currToken = tokens[t];
            i = searchAdTable(currToken);
            if(i == 5 || i == 2){
                if(i==2) fd.write("-x-"+ " (AD," + String.format("%02d", i) + ")\n");
                POOLTAB[poolcnt] = proclit;
                poolcnt++;
                for(j=proclit; j<litcnt; j++){
                    LITTAB[j].addr = lc;
                    fd.write(lc + " (DL,01) (C," + LITTAB[j].lit + ")\n");
                    // if(j<litcnt-1) fd.write("\n");
                    lc++;
                }
                proclit = litcnt;
                continue;
            }

            if(i==1){ // START | ORIGIN : Update the Location Counter
                lc = Integer.parseInt(tokens[t+1]);
                fd.write("-x-" + " (AD," + String.format("%02d", i) + ") " + "(C," + tokens[1] + ")\n");
                continue;
            }else if(i==3){
                String[] teemp = (tokens[t+1]).split("\\+|\\-");
                j = searchSymbolTable(teemp[0]);
                char sy;
                if(tokens[t+1].contains("+")){
                    sy = '+';
                    lc = SYMTAB[j].addr + Integer.parseInt(teemp[1]); 
                }else{
                    sy='-';
                    lc = SYMTAB[j].addr - Integer.parseInt(teemp[1]);
                }
                fd.write("-x-" + " (AD," + String.format("%02d", i) + ") " + "(S," + (j+1) + ")" + sy + teemp[1] + "\n");
                continue;
            }

            if(i==4){
                j = searchSymbolTable(curLabel);

                if(tokens[t+1].contains("+") || tokens[t+1].contains("-")){
                    String[] teemp = (tokens[t+1]).split("\\+|\\-");
                    k = searchSymbolTable(teemp[0]);
                    if(tokens[t+1].contains("+")){
                        SYMTAB[j].addr = SYMTAB[k].addr + Integer.parseInt(teemp[1]);
                    }
                    else{
                        SYMTAB[j].addr = SYMTAB[k].addr - Integer.parseInt(teemp[1]);
                    }
                }else{
                    k = searchSymbolTable(tokens[t+1]);
                    SYMTAB[j].addr = SYMTAB[k].addr;
                }
                fd.write("\n");
                continue;
            }

            i = searchDlTable(currToken);
            if(i!=-1){   
                if(tokens[t+1].contains("\'")){
                    tokens[t+1] = tokens[t+1].substring(1, tokens[t+1].length()-1);
                } 
                fd.write(lc + " (DL," + String.format("%02d", i) + ") " + "(C," + tokens[t+1] + ")\n");
                
                if(i==2)
                    lc += Integer.parseInt(tokens[2]);
                else lc++;

                continue;
            }

            i = searchOpcodeTable(currToken);
            if(i!=-1){
                if(i==0){ // STOP
                    fd.write(lc + " (IS," + String.format("%02d", i) + ")\n");

                }else if(i==9 || i==10){ // READ | PRINT 
                    j = searchSymbolTable(tokens[t+1]);

                    if(j==-1){
                        SYMTAB[symcount].sym = tokens[t+1];
                        symcount++;
                        fd.write(lc + " (IS," + String.format("%02d", i) + ") " + "(S," + symcount + ")\n");
                    }else{
                        fd.write(lc + " (IS," + String.format("%02d", i) + ") " + "(S," + (j+1) + ")\n");
                    }
                }else{
                    if(i==7)
                        j = searchCondTable(tokens[t+1]); // BC GT, LOOP
                    else
                        j = searchRegTable(tokens[t+1]); // SUB AREG,='1'

                    if(tokens[t+2].charAt(0) == '='){ // If Second operand is literal
                        String temp = tokens[t+2].substring(2, tokens[t+2].length()-1);
                        LITTAB[litcnt].lit = temp;
                        litcnt++;
                        fd.write(lc + " (IS," + String.format("%02d", i) + ") " + "(" + j + ") " + "(L," + litcnt + ")\n");
                    }else{ // Second Operand is an Symbol
                        k = searchSymbolTable(tokens[t+2]);
                        if(k==-1){
                            SYMTAB[symcount].sym = tokens[t+2];
                            symcount++;

                            fd.write(lc + " (IS," + String.format("%02d", i) + ") " + "(" + j + ") " + "(S," + symcount + ")\n");
                        }else{
                            fd.write(lc + " (IS," + String.format("%02d", i) + ") " + "(" + j + ") " + "(S," + (k+1) + ")\n");
                        }
                    }
                }
                lc++;
                continue;
            }
        }
        fs.close();
        fd.close();
    }

    private static void generateSymbolTable() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("symtab.txt"));
        bw.write(String.format("%-6s %-15s %-10s\n", "Sr.No", "Symbol", "Address"));
        for (int i = 0; i < symcount; i++) {
            bw.write(String.format("%-6d %-15s %-10d\n", (i + 1), SYMTAB[i].sym, SYMTAB[i].addr));
        }
        bw.close();
    }


    private static void generateLiteralTable() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("littab.txt"));
        bw.write(String.format("%-6s %-15s %-10s\n", "Sr.No", "Literal", "Address"));
        for (int i = 0; i < litcnt; i++) {
            bw.write(String.format("%-6d %-15s %-10d\n", (i + 1), LITTAB[i].lit, LITTAB[i].addr));
        }
        bw.close();
    }


    private static void generatePoolTable() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("pooltab.txt"));
        bw.write("Literal No.\n");
        for (int i = 0; i < poolcnt; i++) {
            bw.write((POOLTAB[i] + 1) + "\n"); // +1 to make it 1-based indexing
        }
        bw.close();
    }


    public static void main(String[] args) throws IOException{
        System.out.println("Pass1 Processing...");
        
        init();

        generateIC();

        generateSymbolTable();

        generateLiteralTable();
        
        generatePoolTable();

        System.out.println("Pass1 Completed!");

    }
}