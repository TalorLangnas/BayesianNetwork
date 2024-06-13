import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPT {
    private Map<String, List<String>> variablesAndOutcomes;
    private Map<List<String>, Double> cpt;
    private List<Double> probabilities;

    @Override
    public String toString() {
        return "CPT{" +
                "variablesAndOutcomes=" + variablesAndOutcomes + "\n" +
                ", cpt=" + cpt +
                '}';
    }

    public CPT() {
        this.variablesAndOutcomes = new HashMap<>();
        this.cpt = new HashMap<>();
        this.probabilities = new ArrayList<>();
    }

    public void addVariablesAndOutcomes(String nodeName, List<String> outcomes) {
        this.variablesAndOutcomes.put(nodeName, outcomes);
    }

    public Map<String, List<String>> getVariablesAndOutcomes() {
        return variablesAndOutcomes;
    }

    public void setVariablesAndOutcomes(Map<String, List<String>> variablesAndOutcomes) {
        this.variablesAndOutcomes = variablesAndOutcomes;
    }

    public List<Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(String probabilities) {
        this.probabilities = convertStringToDoubleList(probabilities);
    }

    private List<Double> convertStringToDoubleList(String input) {
        List<Double> result = new ArrayList<>();

        // Split the input string by commas
        String[] parts = input.split(" ");

        // Convert each part to a Double and add to the result list
        for (String part : parts) {
            try {
                // Trim to remove any leading or trailing whitespace
                double value = Double.parseDouble(part.trim());
                result.add(value);
            } catch (NumberFormatException e) {
                // Handle the case where a part is not a valid double
                System.out.println("Invalid number format: " + part);
            }
        }

        return result;
    }

    public void setCpt(Map<List<String>, Double> cpt) {
        this.cpt = cpt;
    }

    public Map<List<String>, Double> getCpt() {
        return cpt;
    }

    public void buildCpt() {
        Map<List<String>, Double> cpt = generateCpt();
        this.setCpt(cpt);
    }

    private Map<List<String>, Double> generateCpt() {
        Map<List<String>, Double> cpt = new HashMap<>();
        List<List<String>> outcomesList = new ArrayList<>();
        // iterate over variablesAndOutcomes Map and append the outcomes to the variablesOutcomes list
        for (Map.Entry<String, List<String>> entry : this.variablesAndOutcomes.entrySet()) {
            String varName = entry.getKey(); // debug
            outcomesList.add(entry.getValue());
        }
        // then create the sample space with SampleSpaceGenerator
        List<String> sampleSpace = SampleSpaceGenerator.getSampleSpace(outcomesList);
        // create a new Factor object with the sample space and the probabilities
        for (int i = 0; i < this.probabilities.size(); i++) {
//            List<String> key = new ArrayList<>();
            String outcomes = sampleSpace.get(i);
            List<String> outcomesToList = List.of(outcomes.split(" "));
//            key.add(sampleSpace.get(i));
//            key.add(outcomesToList);
//            cpt.put(key, this.probabilities.get(i));
            cpt.put(outcomesToList, this.probabilities.get(i));
        }
        return cpt;
    }

    public Factor toFactor() {
        List<String> variables = new ArrayList<>(variablesAndOutcomes.keySet());
        Map<List<String>, Double> table = new HashMap<>(cpt);
        return new Factor(variables, table);
    }
}