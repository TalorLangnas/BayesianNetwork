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
}
