
import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class MacroPass1 {

    public static void main(String[] args) throws IOException {

        System.out.println("Processing Start..........");

        BufferedReader br = new BufferedReader(new FileReader("macro_input.asm"));

        FileWriter mnt = new FileWriter("MNT.txt");
        FileWriter mdt = new FileWriter("MDT.txt");
        FileWriter kpdt = new FileWriter("KPDT.txt");
        FileWriter pnt = new FileWriter("PNT.txt");
        FileWriter ir = new FileWriter("Intermediate.txt");

        String line;
        String Macroname = null;
        LinkedHashMap<String, Integer> pntab = new LinkedHashMap<>();
        int paramno = 1, mdtp = 1, kpdtp = 0, pp = 0, kp = 0, flag = 0;

        while ((line = br.readLine()) != null) {

            String parts[] = line.split("\\s+");

            if (parts[0].equalsIgnoreCase("MACRO")) {
                line = br.readLine();
                parts = line.split("\\s+");
                Macroname = parts[0];
                flag = 1;

                if (parts.length <= 1) {
                    mnt.write(parts[0] + "\t" + pp + "\t" + kp + "\t" + mdtp + "\t" + (kpdtp == 0 ? kpdtp : (kpdtp + 1))
                            + "\n");
                    continue;

                }

                for (int i = 1; i < parts.length; i++) {

                    parts[i] = parts[i].replaceAll("[&,]", "");

                    if (parts[i].contains("=")) {
                        kp++;
                        String keywordParam[] = parts[i].split("=");
                        pntab.put(keywordParam[0], paramno++);
                        if (keywordParam.length == 2) {
                            kpdt.write(keywordParam[0] + "\t" + keywordParam[1] + "\n");

                        } else {
                            kpdt.write(keywordParam[0] + "\t - \n");
                        }
                    } else {
                        pp++;
                        pntab.put(parts[i], paramno++);
                    }

                }

                mnt.write(parts[0] + "\t" + pp + "\t" + kp + "\t" + mdtp + "\t" + (kp == 0 ? kpdtp : (kpdtp + 1))
                        + "\n");
                kpdtp = kp + kpdtp;

            } else if (parts[0].equalsIgnoreCase("MEND")) {

                mdt.write(line + '\n');
                mdtp++;
                flag = kp = pp = 0;
                paramno = 1;
                pnt.write(Macroname + ":\t");
                Iterator<String> itr = pntab.keySet().iterator();
                while (itr.hasNext()) {
                    pnt.write(itr.next() + "\t");

                }
                pnt.write("\n");
                pntab.clear();

            } else if (flag == 1) {

                for (int i = 0; i < parts.length; i++) {

                    if (parts[i].contains("&")) {
                        parts[i] = parts[i].replaceAll("[&,]", "");
                        mdt.write("(P," + pntab.get(parts[i]) + ")\t");
                    } else {
                        mdt.write(parts[i] + "\t");
                    }

                }
                mdt.write("\n");
                mdtp++;

            } else {
                ir.write(line + "\n");
            }

        }

        br.close();
        mnt.close();
        mdt.close();
        ir.close();
        kpdt.close();
        pnt.close();
        System.out.println("Processing complte......");

    }

}
