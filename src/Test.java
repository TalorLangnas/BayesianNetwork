//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class Test {
//    public static void main(String[] args) throws Exception {
//        BufferedReader br = new BufferedReader(new FileReader("input.txt"));
//        BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
//        String networkXmlFile = br.readLine().trim();
//        BayesianNetworkBuilder builder = new BayesianNetworkBuilder();
//        BayesianNetwork network = builder.buildNetworkFromXML(networkXmlFile);
//        network.printNetwork();
//
//        BayesBall bayesBall = new BayesBall(network);
//        VariableElimination variableElimination = new VariableElimination(network);
//
//        // Read the queries from the input file
//        String line;
//        while ((line = br.readLine()) != null) {
//            // Variable elimination query
//            if (line.startsWith("P(")) {
//                // Split the line into 2 parts:
//                // parts[0] contains the actual query, e.g., "P(Q=q|E1=e1,E2=e2,...,Ek=ek)"
//                // parts[1] (if it exists) contains the elimination order, e.g., "H1-H2-...-Hj"
//                String[] parts = line.split(" ");
//                // Extract the Query Part: "Q=q|E1=e1,E2=e2,...,Ek=ek"
//                String queryPart = parts[0].substring(2, parts[0].length() - 1);
//                // Split the Query into Query Variable and Evidence: "Q=q" and "E1=e1,E2=e2,...,Ek=ek"
//                String[] queryParts = queryPart.split("\\|");
//                // Extract the Query Variable and Its Value:
//                String queryVariable = queryParts[0].split("=")[0];
//                String queryValue = queryParts[0].split("=")[1];
//                // Extract the Evidence
//                Map<String, String> evidence = new HashMap<>();
//                if (queryParts.length > 1 && !queryParts[1].isEmpty()) {
//                    String[] evidenceParts = queryParts[1].split(",");
//                    for (String ev : evidenceParts) {
//                        String[] evParts = ev.split("=");
//                        evidence.put(evParts[0], evParts[1]);
//                    }
//                }
//                // Extract the Elimination Order
//                List<String> eliminationOrder = Arrays.asList(parts[1].split("-"));
//                System.out.println("Elimination Order: " + eliminationOrder); // debug
//                // Perform Variable Elimination
//                List<Factor> factors = variableElimination.testRemoveEvidenceVariable(queryVariable + "=" + queryValue, evidence, eliminationOrder);
////                for (Factor factor : factors) {
////                    System.out.println(factor);
////                }
//
//                String result = variableElimination.eliminate(queryVariable + "=" + queryValue, evidence, eliminationOrder);
//                //                bw.write(result);
//            } else {
//                System.out.println("Invalid query format: " + line);
//            }
//        }
//    }
//}