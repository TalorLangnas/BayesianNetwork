import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    public Factor join(BayesianNetwork network, Factor other, String variable, AtomicInteger mulCounter) {
        // 1. Create new CPT
        CPT newCPT = new CPT();
        // 2. add variables  and their outcomes to the new CPT
        for(String var : this.variables){
            newCPT.addVariablesAndOutcomes(var, network.getNode(var).getOutcomes());
        }
        for(String var : other.variables){
            if(!newCPT.getVariables().contains(var)){
                newCPT.addVariablesAndOutcomes(var, network.getNode(var).getOutcomes());
            }
        }
        // 3. generate sampleSpace for the new CPT
        newCPT.generateSampleSpace();

        // 4. Iterate through newCPT keys
        for (Map.Entry<List<String>, Double> entry : newCPT.getCpt().entrySet()) {
            List<String> newSample = entry.getKey();
            // extract the value from thisFactor
            Double value1 = this.extractValue(newCPT, newSample);
            // extract the value from otherFactor
            Double value2 = other.extractValue(newCPT, newSample);
            newCPT.getCpt().put(newSample, value1 * value2);
            // Increment multiplication counter
            mulCounter.incrementAndGet();
        }

        return newCPT.toFactor();
    }
    private Double extractValue(CPT cpt, List<String> sample){
        // extract the value from thisFactor
        Double res = null;
        Map<List<String>, Double> tmpCpt = new HashMap<>(this.getCpt());
        // for each variable in newCpt
        for(String var : cpt.getVariables()){
            // if the variable located in the factor variable list
            if(this.getVariables().contains(var)){
                // keep the keys that have the same value as the variable value
                restrictVariable(tmpCpt, var, sample.get(cpt.getVariables().indexOf(var)));
            }
        }

        // get the value of the key in tmpCpt1
        for (Map.Entry<List<String>, Double> entry : tmpCpt.entrySet()){
            res = tmpCpt.get(entry.getKey());
        }
        return res;
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
            /*
            Increment sum counter for each sum operation:
            If newCpt.get(reducedAssignment) returns null, it means that the 'reducedAssignment' key
            does not exist in the map. In this case, we assign 0.0 to the value of the key "reducedAssignment"
            and there is no need to increment the sumCounter.
            If newCpt.get(reducedAssignment) is not null, it means that the 'reducedAssignment' key
            exists in the map. In this case, we assign the value newCpt.get(reducedAssignment) + entry1.getValue()
            to the key "reducedAssignment", and then we have to increment the sumCounter.
             */
            if(newCpt.get(reducedAssignment) != null){
                sumCounter.incrementAndGet();
            }
            newCpt.put(reducedAssignment, newCpt.getOrDefault(reducedAssignment, 0.0) + entry1.getValue());
        }
        // Update the factor with the new variables and CPT
        this.variables = newVariables;
        this.cpt = newCpt;
    }

    public void normalize(AtomicInteger sumCounter) {
        double sum = 0.0;
        for (double value : this.cpt.values()) {
            if(sum != 0){
                sumCounter.incrementAndGet();
            }
            sum += (value);
        }
        for (Map.Entry<List<String>, Double> entry : this.cpt.entrySet()) {
            entry.setValue(entry.getValue() / sum);
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
            List<String> key = entry.getKey();
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

    //  restrictVariable removes entries from the CPT that do not match the assignment
    public void restrictVariable(Map<List<String>, Double> cpt, String variable, String assignment) {
        int variableIndex = variables.indexOf(variable);
        if (variableIndex == -1) {
            // The variable is not in the list
            return;
        }
        // Remove entries from cpt that do not match the assignment
        List<List<String>> keysToRemove = new ArrayList<>();
        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            List<String> key = entry.getKey();
            if (!key.get(variableIndex).equals(assignment)) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (List<String> key : keysToRemove) {
            cpt.remove(key);
        }
    }

    // this functions checks if the factor holds all the data needed to extract the result without any further operations
    // case 1: the factor contains the query variable and all the evidence variables with no other hidden variables
    public boolean checkQueryAndEvidence(String queryVariable,Map<String, String> evidenceVars, List<String> hidden){
        // Check if the Factor contains hidden variables
        for(String hiddenVar : hidden){
            if(variables.contains(hiddenVar)){
                return false;
            }
        }
        // Check if the Factor contains the query variable
        if (!variables.contains(queryVariable)) {
            return false;
        }
        // Check if the Factor contains the evidence variables
        for (String evidenceVar : evidenceVars.keySet()) {
            if (!variables.contains(evidenceVar)) {
                return false;
            }
        }
        return true;
    }
}



