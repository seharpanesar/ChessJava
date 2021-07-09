package TestFiles;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DebugHelp {
    public static void main(String[] args) throws IOException {
        DebugHelp debugHelp = new DebugHelp();

        debugHelp.fileCheck();


    }

    private void fileCheck() throws IOException {
        DebugHelp debugHelp = new DebugHelp();
        HashMap<String,String> map1 = debugHelp.parseFile("C:/Users/sehar/IdeaProjects/ChessPart2/src/TestFiles/DebugFile1");
        HashMap<String,String> map2 = debugHelp.parseFile("C:/Users/sehar/IdeaProjects/ChessPart2/src/TestFiles/DebugFile2");

        if (map1.size() != map2.size()) {
            System.out.println("Different size:");
            findMissing(map1, map2);

        } else {
            mapCheck(map1, map2);
        }


    }

    private void findMissing(HashMap<String, String> map1, HashMap<String, String> map2) {
        HashMap<String, String> largerMap = (map1.size() > map2.size()) ? map1 : map2;
        HashMap<String, String> smallerMap = (map1.size() > map2.size()) ? map2 : map1;

        for (Map.Entry<String, String> entry : largerMap.entrySet()) {
            String val1 = entry.getKey();
            if (smallerMap.get(val1) == null) {
                System.out.println(val1 + " is not in smaller output");
            }
        }
    }

    private void mapCheck(HashMap<String, String> map1, HashMap<String, String> map2) {
        boolean diffFlag = false;
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            String val1 = entry.getValue();
            String val2 = map2.get(entry.getKey());
            int val1int = Integer.parseInt(val1);
            int val2int = Integer.parseInt(val2);
            int diff = Math.abs(val1int - val2int);
            if (!val1.equals(val2)) {
                System.out.println("differs at " + entry.getKey() + "by" + diff);
                diffFlag = true;
            }
        }
        if (!diffFlag) {
            System.out.println("All looks similar");
        }

    }


    private HashMap<String, String> parseFile(String s) throws IOException {
        HashMap<String, String> map = new HashMap<>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(s)));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] parts = line.split(" ");
            map.put(parts[0], parts[1]);
        }
        return map;
    }


}
