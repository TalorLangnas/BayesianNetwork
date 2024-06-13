import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

//public class Factor implements FactorPlan{
public class Factor implements Comparable<Factor> {
    private List<String> variables;
    private Map<List<String>, Double> cpt;

    // Constructors:
    public Factor() {
        this.variables = new ArrayList<>();
        this.cpt = new HashMap<>();
    }

    public Factor(List<String> variables, Map<List<String>, Double> cpt) {
        this.variables = new ArrayList<>(variables);
        this.cpt = new HashMap<>(cpt);
    }

    @Override
    public String toString() {
        return "Factor{" +
                "variables=" + variables +
                ", cpt=" + cpt +
                '}';
    }

    @Override
    public int compareTo(Factor other) {
        // First, compare by the size of the cpt keys
        int sizeComparison = Integer.compare(this.cpt.size(), other.cpt.size());
        if (sizeComparison != 0) {
            return sizeComparison;
        }

        // If the sizes are equal, compare by the ASCII value of all the values in List<String> variables
        String thisVariablesConcatenated = String.join("", this.variables);
        String otherVariablesConcatenated = String.join("", other.variables);
        return thisVariablesConcatenated.compareTo(otherVariablesConcatenated);
    }

    // Getters and Setters:
    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public Map<List<String>, Double> getCpt() {
        return cpt;
    }

    public void setCpt(Map<List<String>, Double> cpt) {
        this.cpt = cpt;
    }

    // Operations:

    public Factor join(Factor other, String variable, AtomicInteger mulCounter) {
        // Create a new list of variables for the resulting factor
        List<String> newVariables = new ArrayList<>(this.variables);
        for (String var : other.variables) {
            if (!newVariables.contains(var)) {
                newVariables.add(var);
            }
        }

        // Create a new CPT for the resulting factor
        Map<List<String>, Double> newCpt = new HashMap<>();

        // Iterate through all possible combinations of variable assignments
        for (Map.Entry<List<String>, Double> entry1 : this.cpt.entrySet()) {
            for (Map.Entry<List<String>, Double> entry2 : other.cpt.entrySet()) {
                if (consistent(entry1.getKey(), entry2.getKey(), variable, other)) {
                    List<String> newAssignment = mergeAssignments(entry1.getKey(), entry2.getKey(), newVariables, other);
                    double newProb = entry1.getValue() * entry2.getValue();
                    newProb = roundFiveDigits(newProb);
//                    newProb = Math.floor(newProb * 100000.0) / 100000.0;
                    newCpt.put(newAssignment, newProb);
                    // Increment multiplication counter using AtomicInteger
                    mulCounter.incrementAndGet();
                }
            }
        }

        return new Factor(newVariables, newCpt);
    }

    private boolean consistent(List<String> assignment1, List<String> assignment2, String variable, Factor other) {
        int index1 = this.variables.indexOf(variable);
        int index2 = other.variables.indexOf(variable);
        return assignment1.get(index1).equals(assignment2.get(index2));
    }

    private List<String> mergeAssignments(List<String> assignment1, List<String> assignment2, List<String> newVariables, Factor other) {
        List<String> newAssignment = new ArrayList<>(Collections.nCopies(newVariables.size(), ""));
        for (int i = 0; i < this.variables.size(); i++) {
            newAssignment.set(newVariables.indexOf(this.variables.get(i)), assignment1.get(i));
        }
        for (int i = 0; i < other.getVariables().size(); i++) {
            newAssignment.set(newVariables.indexOf(other.getVariables().get(i)), assignment2.get(i));
        }
        return newAssignment;
    }

    public void eliminate(String variable, AtomicInteger sumCounter) {
        // Identify the index of the variable to eliminate
        int indexToEliminate = this.variables.indexOf(variable);
        if (indexToEliminate == -1) {
            throw new IllegalArgumentException("Variable not found in the factor.");
        }

        // Create a new list of variables excluding the one to eliminate
        List<String> newVariables = new ArrayList<>(this.variables);
        newVariables.remove(variable);

        // Create a new CPT for the resulting factor
        Map<List<String>, Double> newCpt = new HashMap<>();

        // Iterate through all possible combinations of variable assignments
        for (Map.Entry<List<String>, Double> entry1 : this.cpt.entrySet()) {
            List<String> reducedAssignment = new ArrayList<>(entry1.getKey());
            reducedAssignment.remove(indexToEliminate);
            newCpt.put(reducedAssignment, roundFiveDigits(newCpt.getOrDefault(reducedAssignment, 0.0) + entry1.getValue()));
            // Increment sum counter for each sum operation
            sumCounter.incrementAndGet();
        }

        // Update the factor with the new variables and CPT
        this.variables = newVariables;
        this.cpt = newCpt;
    }

    public void normalize(AtomicInteger sumCounter) {
        double sum = 0.0;
        for (double value : this.cpt.values()) {
//            sum += (value);
            sum = roundFiveDigits(sum + value);
            sumCounter.incrementAndGet();
        }

        for (Map.Entry<List<String>, Double> entry : this.cpt.entrySet()) {
            entry.setValue(roundFiveDigits(entry.getValue() / sum));
        }
    }


    public void removeEvidenceVariable(String variable, String assignment) {
        int variableIndex = variables.indexOf(variable);
        if (variableIndex == -1) {
            // The variable is not in the list
            return;
        }

        // Remove entries from cpt that do not match the assignment
        List<List<String>> keysToRemove = new ArrayList<>();
        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            // processing the key
//            List<String> key1 = entry.getKey();
//            String key2 = key1.get(0);
//            List<String> key3 = List.of(key2.split(" "));
            List<String> key = entry.getKey();
//            String assignmentInVariableIndex = key3.get(variableIndex);
//            if (!assignmentInVariableIndex.equals(assignment)) {
            if (!key.get(variableIndex).equals(assignment)) {
                keysToRemove.add(entry.getKey());
            }
        }
            for (List<String> key : keysToRemove) {
                cpt.remove(key);
            }

            // Update the keys in cpt to remove the variable's value
            Map<List<String>, Double> updatedCpt = new HashMap<>();

            for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
                List<String> newKey = new ArrayList<>(entry.getKey());
                newKey.remove(variableIndex);
                updatedCpt.put(newKey, entry.getValue());
            }
            this.cpt = updatedCpt;

            // Remove the variable from the variables list
            variables.remove(variableIndex);
    }

    public double roundFiveDigits(double x){
        return Math.floor(x * 100000.0) / 100000.0;
    }

}



