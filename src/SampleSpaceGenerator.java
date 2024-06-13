import java.util.ArrayList;
import java.util.List;

public class SampleSpaceGenerator {

    public static List<String> getSampleSpace(List<List<String>> variables) {
        List<String> sampleSpace = new ArrayList<>();
        if (variables == null || variables.isEmpty()) {
            return sampleSpace;
        }
        generateCombinations(variables, 0, "", sampleSpace);
        return sampleSpace;
    }

    private static void generateCombinations(List<List<String>> variables, int index, String current, List<String> sampleSpace) {
        if (index == variables.size()) {
            sampleSpace.add(current.trim());
            return;
        }

        List<String> currentVariableOutcomes = variables.get(index);
        for (String outcome : currentVariableOutcomes) {
            generateCombinations(variables, index + 1, current + " " + outcome, sampleSpace);
        }
    }

    public static void main(String[] args) {
        // Example usage
        List<List<String>> variables = new ArrayList<>();
        variables.add(List.of("A1", "A2", "A3"));
        variables.add(List.of("B1", "B2"));
        variables.add(List.of("C1", "C2", "C3"));

        List<String> sampleSpace = getSampleSpace(variables);

        // Print the sample space
        for (String sample : sampleSpace) {
            System.out.println(sample);
        }
    }
}
