import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CPT {
        private List<String> variables;
        private List<List<String>> outcomes;
        private Map<List<String>, Double> cpt;
    private List<Double> probabilities;


    public CPT() {
        this.variables = new ArrayList<>();
        this.outcomes = new ArrayList<>();
        this.cpt = new HashMap<>();
        this.probabilities = new ArrayList<>();
    }

    @Override
    public String toString() {
        String str = "CPT {\n";
        for(int i = 0; i < variables.size(); i++) {
            str += "[" + variables.get(i) + "]" + "[" + outcomes.get(i) + "]\n";
        }
        str += "cpt=" + cpt + "\n";
        return str;
    }

    public void addVariablesAndOutcomes(String nodeName, List<String> outcomes) {
//        this.variablesAndOutcomes.put(nodeName, outcomes); // original
        this.variables.add(nodeName);
        this.outcomes.add(outcomes);
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public List<List<String>> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<List<String>> outcomes) {
        this.outcomes = outcomes;
    }
//    public Map<String, List<String>> getVariablesAndOutcomes() {
//        return variablesAndOutcomes;
//    }
//
//    public void setVariablesAndOutcomes(Map<String, List<String>> variablesAndOutcomes) {
//        this.variablesAndOutcomes = variablesAndOutcomes;
//    }

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
        List<String> sampleSpace = SampleSpaceGenerator.getSampleSpace(this.outcomes);
        // create a new Factor object with the sample space and the probabilities
        for (int i = 0; i < this.probabilities.size(); i++) {
            String outcomes = sampleSpace.get(i);
            List<String> outcomesToList = List.of(outcomes.split(" "));
            cpt.put(outcomesToList, this.probabilities.get(i));
        }
        return cpt;
    }
    // function to assign sample space to the CPT considering the variables and outcomes
    public void generateSampleSpace(){
        List<String> sampleSpace = SampleSpaceGenerator.getSampleSpace(this.outcomes);
        for (int i = 0; i < sampleSpace.size(); i++){
            String outcomes = sampleSpace.get(i);
            List<String> outcomesToList = List.of(outcomes.split(" "));
            cpt.put(outcomesToList, 0.0);
        }
    }

    public Factor toFactor() {
//        List<String> variables = new ArrayList<>(variablesAndOutcomes.keySet());
        Map<List<String>, Double> table = new HashMap<>(this.cpt);
        return new Factor(this.variables, table);
    }
}
