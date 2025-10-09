import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Pass2 {

    static List<List<String>> srcLines = new ArrayList<>();
    static List<MNTEntry> mnt = new ArrayList<>();
    static List<List<String>> mdt = new ArrayList<>();
    static List<KPDTEntry> kpdt = new ArrayList<>();

    static List<String> apt = new ArrayList<>();
    static List<String> outputLines = new ArrayList<>();

    public static void readSrc() throws IOException {
        Path path = Paths.get("src.txt");  // Make sure this file exists in current dir
        List<String> lines = Files.readAllLines(path);
        srcLines.clear();
        for (String line : lines) {
            srcLines.add(Arrays.asList(line.trim().split("\\s+")));
        }
    }

    public static void readMnt() throws IOException {
        Path path = Paths.get("MNT.txt");
        List<String> lines = Files.readAllLines(path);
        mnt.clear();
        boolean firstLine = true;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            if (firstLine) {  // Skip header line
                firstLine = false;
                continue;
            }
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length < 5) continue; // safety check
            String macroName = tokens[0];
            int pp = Integer.parseInt(tokens[1]);
            int kp = Integer.parseInt(tokens[2]);
            int mdtp = Integer.parseInt(tokens[3]);
            int kpdtp = Integer.parseInt(tokens[4]);
            mnt.add(new MNTEntry(macroName, pp, kp, mdtp, kpdtp));
        }
    }

    public static void readMdt() throws IOException {
        Path path = Paths.get("MDT.txt");
        List<String> lines = Files.readAllLines(path);
        mdt.clear();
        for (String line : lines) {
            if(line.trim().isEmpty()) continue;
            mdt.add(Arrays.asList(line.trim().split("\\s+")));
        }
    }

    public static void readKpdt() throws IOException {
        Path path = Paths.get("KPDT.txt");
        List<String> lines = Files.readAllLines(path);
        kpdt.clear();
        boolean firstLine = true;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            if (firstLine) {  // Skip header line
                firstLine = false;
                continue;
            }
            String[] tokens = line.trim().split("\\s+");
            if(tokens.length < 2) continue; // safety check
            kpdt.add(new KPDTEntry(tokens[0], tokens[1]));
        }
    }

    public static Optional<MNTEntry> isMacroCall(List<String> line) {
        int kpCount = 0;
        int ppCount = 0;

        // Count positional and keyword parameters in the macro call
        for (int i = 1; i < line.size(); i++) {
            if (line.get(i).contains("=")) {
                kpCount++;
            } else {
                ppCount++;
            }
        }

        for (MNTEntry entry : mnt) {
            if (line.get(0).equals(entry.macroName) && ppCount == entry.pp && kpCount <= entry.kp) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static List<String> getParameters(List<String> calledLine, MNTEntry mntEntry) {
        List<String> params = new ArrayList<>();

        // Add positional parameters in order
        for (int i = 1; i < calledLine.size(); i++) {
            if (!calledLine.get(i).contains("=")) {
                params.add(calledLine.get(i));
            }
        }

        int kp = mntEntry.kp;
        int kpdtStart = mntEntry.kpdtp - 1;

        // Copy default keyword parameters from KPDT
        List<KPDTEntry> keywordParams = new ArrayList<>();
        for (int i = 0; i < kp; i++) {
            KPDTEntry kpdtEntry = kpdt.get(kpdtStart + i);
            keywordParams.add(new KPDTEntry(kpdtEntry.paramName, kpdtEntry.defaultValue));
        }

        // Override defaults with provided keyword arguments
        for (int i = 1; i < calledLine.size(); i++) {
            if (calledLine.get(i).contains("=")) {
                String[] tokens = calledLine.get(i).split("=");
                String parameter = tokens[0];
                String argument = tokens[1];
                for (KPDTEntry kpdtEntry : keywordParams) {
                    if (kpdtEntry.paramName.equals(parameter)) {
                        kpdtEntry.defaultValue = argument;
                        break;
                    }
                }
            }
        }

        // Add final keyword parameter values to params
        for (KPDTEntry kpdtEntry : keywordParams) {
            if (!kpdtEntry.defaultValue.equals("_")) {
                params.add(kpdtEntry.defaultValue);
            } else {
                throw new RuntimeException("Argument not provided for parameter " + kpdtEntry.paramName);
            }
        }

        return params;
    }

    public static void expandMacroCall(int mdtStart) {
        int i = mdtStart;
        while (i < mdt.size()) {
            List<String> mdtEntry = mdt.get(i);
            if (mdtEntry.contains("MEND")) {
                break;
            }

            StringBuilder outputLine = new StringBuilder();
            for (String token : mdtEntry) {
                if (token.startsWith("(") && token.endsWith(")")) {
                    // Expected format: (P,index)
                    String inside = token.substring(token.indexOf('(') + 1, token.indexOf(')'));
                    String[] parts = inside.split(",");
                    int index = Integer.parseInt(parts[1].trim()) - 1;
                    outputLine.append(apt.get(index)).append(" ");
                } else {
                    outputLine.append(token).append(" ");
                }
            }
            outputLines.add(outputLine.toString().trim());
            i++;
        }
    }

    public static void main(String[] args) throws IOException {
        readSrc();
        readMnt();
        readMdt();
        readKpdt();

        for (List<String> line : srcLines) {
            Optional<MNTEntry> maybeMntEntry = isMacroCall(line);
            if (maybeMntEntry.isPresent()) {
                MNTEntry mntEntry = maybeMntEntry.get();
                apt = getParameters(line, mntEntry);
                int mdtStart = mntEntry.mdtp - 1;
                expandMacroCall(mdtStart);
            } else {
                outputLines.add(String.join(" ", line));
            }
        }

        // Print the expanded output
        for (String line : outputLines) {
            System.out.println(line);
        }
    }

    // MNT Entry class
    static class MNTEntry {
        String macroName;
        int pp;    // positional parameters
        int kp;    // keyword parameters
        int mdtp;  // MDT pointer (starting line in MDT)
        int kpdtp; // KPDT pointer (starting line in KPDT)

        public MNTEntry(String macroName, int pp, int kp, int mdtp, int kpdtp) {
            this.macroName = macroName;
            this.pp = pp;
            this.kp = kp;
            this.mdtp = mdtp;
            this.kpdtp = kpdtp;
        }
    }

    // KPDT Entry class
    static class KPDTEntry {
        String paramName;
        String defaultValue;

        public KPDTEntry(String paramName, String defaultValue) {
            this.paramName = paramName;
            this.defaultValue = defaultValue;
        }
    }
}
