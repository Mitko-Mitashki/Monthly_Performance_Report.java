import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.json.*;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class Monthly_Performance_Reports {
    public static void main(String[] args) throws IOException, ParseException {
        String data = "";
        String reportDef = "";
        String resultPath = "";
        if (args.length > 0) {
            data = args[0];
            reportDef = args[1];
            resultPath = args[2];
            System.out.println("Path for Data file: " + data);
            System.out.println("Path for Report Definition file: " + reportDef);
            System.out.println("Path for Result file: " + resultPath);
        } else {
            System.out.println("You have to provide path for Data file, Report Definition file and Result file in command line.");
        }

        JSONParser jsonparserD = new JSONParser();
        JSONParser jsonparser = new JSONParser();

        FileReader dataReader = new FileReader(data);
        FileReader defReader = new FileReader(reportDef);

        Object dataObj = jsonparserD.parse(dataReader);
        Object defObj = jsonparser.parse(defReader);

        JSONObject empobj = (JSONObject) dataObj;
        JSONObject defobj = (JSONObject) defObj;
        JSONArray jsonarray = (JSONArray) empobj.get("Data");

        long topPerformersThreshold = (long) defobj.get("topPerformersThreshold");
        boolean useExperienceMultiplier = (boolean) defobj.get("useExperienceMultiplier");
        long periodLimit = (long) defobj.get("periodLimit");
        //System.out.println(topPerformersThreshold);
        //System.out.println(useExperienceMultiplier);
        //System.out.println(periodLimit);

        String name = "";
        long totalSales = 0L;
        long salesPeriod = 0L;
        double experienceMultiplier = 0.0d;
        double score = 0.0d;
        Map<String, Double> map=new HashMap<String, Double>();

        for (int i = 0; i < jsonarray.size(); i++) {
            JSONObject dataE = (JSONObject) jsonarray.get(i);
            //System.out.println("I: " + i);
            name = (String) dataE.get("name");
            totalSales = (long) dataE.get("totalSales");
            salesPeriod = (long) dataE.get("salesPeriod");
            experienceMultiplier = (double) dataE.get("experienceMultiplier");
            //System.out.println("Name: " + name);

            if(useExperienceMultiplier == true) {
                score = (double) totalSales/salesPeriod*experienceMultiplier;
            } else {
                score = (double) totalSales/salesPeriod;
            }
            //System.out.println("Score: " + score);
            if (salesPeriod <= periodLimit) {
                map.put(name, score);
                //System.out.println("Map size: " + map.size());
            }
        }

        Map<String, Double> res = map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        /* for(Map.Entry ml : res.entrySet()) {
            System.out.println("Name: " + ml.getKey()+ " Score: " + ml.getValue());
        } */
        File result = new File(resultPath);
        float count = 0;
        float topX = (float) (topPerformersThreshold * res.size())/100;
        //System.out.println("TopX: " + topX);
        double lastValue = 0.0d;
        for(Map.Entry m : res.entrySet()) {

            if (count >= topX) break;
            lastValue =(double) m.getValue();
            count++;
            //System.out.println("Count: " + count);
        }
        try {
            FileWriter writer = new FileWriter(result, true);
            writer.write("Name, Score\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("Last value: " + lastValue);
        double lastV = lastValue;
        res.forEach(
                (k, v) -> {
                    if (v >= lastV){
                        //System.out.println("Key : " + k + ", Value : " + v);
                        try {
                            FileWriter writer = new FileWriter(result, true);
                            writer.write(k + " , " + v + "\n");
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}