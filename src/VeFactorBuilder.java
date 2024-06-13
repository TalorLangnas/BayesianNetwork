import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//public class VeFactorBuilder implements FactorBuilder{
public class VeFactorBuilder {
    private Factor factor;
//    @Override
//    public void buildFactorName(String name) {
//        factor.setName(name);
//    }
//
//    @Override
//    public void buildVariablesAndOutcomes() {
//        Map<String, List<String>> variablesAndOutcomes = new HashMap<>();
//        factor.setVariablesAndOutcomes(variablesAndOutcomes);
//    }
//
//    @Override
//    public void addVariablesAndOutcomes(String nodeName, List<String> outcomes) {
//        factor.getVariablesAndOutcomes().put(nodeName, outcomes);
//    }
//
//    @Override
//    public void buildCpt() {
//        Map<List<String>, Double> cpt = generateCpt();
//        factor.setCpt(cpt);
//    }
//
//    @Override
//    public Factor getFactor() {
//        return this.factor;
//    }
//
//    public Map<List<String>, Double> generateCpt() {
//        Map<List<String>, Double> cpt = new HashMap<>();
//        Map<String, List<String>> variablesAndOutcomes = factor.getVariablesAndOutcomes();
//        List<Double> probabilities = factor.getProbabilities();
//        List<List<String>> OutcomesList = new ArrayList<>();
//        // iterate over variablesAndOutcomes Map and append the outcomes to the variablesOutcomes list
//        for (Map.Entry<String, List<String>> entry : variablesAndOutcomes.entrySet()) {
//            String varName = entry.getKey(); // debug
//            OutcomesList.add(entry.getValue());
//        }
//        // then create the sample space with SampleSpaceGenerator
//        List<String> sampleSpace = SampleSpaceGenerator.getSampleSpace(OutcomesList);
//        // create a new Factor object with the sample space and the probabilities
//        for (int i = 0; i < probabilities.size(); i++) {
//            List<String> key = new ArrayList<>();
//            key.add(sampleSpace.get(i));
//            cpt.put(key, probabilities.get(i));
//        }
//        return cpt;
//    }
//
//    @Override
//    public void buildProbabilities(String probabilities) {
//        List<Double> probabilityList = convertStringToDoubleList(probabilities);
//        factor.setProbabilities(probabilityList);
//    }
//
//    private List<Double> convertStringToDoubleList(String input) {
//        List<Double> result = new ArrayList<>();
//
//        // Split the input string by commas
//        String[] parts = input.split(" ");
//
//        // Convert each part to a Double and add to the result list
//        for (String part : parts) {
//            try {
//                // Trim to remove any leading or trailing whitespace
//                double value = Double.parseDouble(part.trim());
//                result.add(value);
//            } catch (NumberFormatException e) {
//                // Handle the case where a part is not a valid double
//                System.out.println("Invalid number format: " + part);
//            }
//        }
//
//        return result;
//    }
}
