import org.w3c.dom.css.Counter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VariableElimination {
    private BayesianNetwork network;
    private AtomicInteger sumCounter;
    private AtomicInteger mulCounter;;

    public VariableElimination(BayesianNetwork network) {
        this.network = network;
        this.sumCounter = new AtomicInteger(0); // Initialize with 0
        this.mulCounter = new AtomicInteger(0); // Initialize with 0
    }

    // Getter for sumCounter
    public void zeroCounters() {
        this.sumCounter.set(0);
        this.mulCounter.set(0);
    }

    public Factor join(List<Factor> factors, String varName, AtomicInteger mulCounter){
        while(factors.size() > 1){
            // Extract the first two factors from the list
            Factor f1 = factors.remove(0);
            Factor f2 = factors.remove(0);
            // Join the two factors on the specified variable
            Factor joinedFactor = f1.join(f2, varName, mulCounter);
            // Add the resulting factor back to the list
            factors.add(0, joinedFactor);
            // Sort the factors list
            Collections.sort(factors);
        }
        if(factors.size() == 1){
            return factors.get(0);
        }
        return new Factor();
    }
    // Create function that checks if exist factor that contains the query result,
    // if yes calculate it and return the result as a formatted string
    String checkIfExistResultFactor(String queryVariable, String queryValue, Map<String, String> evidence, List<Factor> factors){
        Map<String, String> variables = new HashMap<>(evidence);
        variables.put(queryVariable, queryValue);
        for(Factor factor : factors){
            double res = factor.checkIfExistResultFactor(variables);
            if(res >= 0){
                return String.format("%.5f", res) + "," + this.sumCounter + "," + this.mulCounter;
            }
        }
        return null;
    }
    public String eliminate(String query, Map<String, String> evidence, List<String> eliminationOrder) {
        // Implement the Variable Elimination Algorithm
        // queryVariable = "Q=q"
        //1.  Parse the query
        String[] queryParts = query.split("=");
        String queryVariable = queryParts[0];
        String queryValue = queryParts[1];
        // 2. get the relevant nodes for the query
        Set<NetNode> relevantNodes = getRelevantNodes(queryVariable, evidence, eliminationOrder);
        // 3. remove the irrelevant nodes from eliminationOrder list
        // Casting eliminationOrder to ArrayList to avoid ConcurrentModificationException
        ArrayList<String> eliminationOrderCopy = new ArrayList<>(eliminationOrder);
        for(String node : eliminationOrder){
            if(!relevantNodes.contains(network.getNode(node))){
//               boolean removed = eliminationOrder.remove(node);
                eliminationOrderCopy.remove(node);
            }
        }
        eliminationOrder = eliminationOrderCopy;
        // 4. Create a list of factors for each node in the network
        List<Factor> factors = new ArrayList<>();
        for(NetNode node : relevantNodes){
            factors.add(node.getCPT().toFactor());
        }
        // Checks if exist factor that contains the query result,
        String checkResult = checkIfExistResultFactor(queryVariable, queryValue, evidence, factors);
        if(checkResult != null){
            return checkResult;
        }
        // 5. Initialize the factors with the relevant entries (with no the evidence variables)
        List<Factor>  factorsToRemove = new ArrayList<>();
        for(Map.Entry<String, String> entry : evidence.entrySet()){
            for(Factor factor : factors){
                if(factor.getVariables().contains(entry.getKey())){
                    factor.removeEvidenceVariable(entry.getKey(), entry.getValue());
                    if(factor.getVariables().size() == 0){
                        factorsToRemove.add(factor);
//                        factors.remove(factor);
                    }
                }
            }
        }
        // Remove factors with no variables
        factors.removeAll(factorsToRemove);
//        System.out.println("> > > > > Factors after reduce: ");
//        for (Factor factor : factors) {
//                    System.out.println(factor);
//                }
        // 6. If we have hidden variables Eliminate them by the elimination order
        for(String variable : eliminationOrder){
            // 6.1 create temp list with the relevant factors
            List<Factor> variableFactors = new ArrayList<>();
            for(Factor factor : factors){
                if(factor.getVariables().contains(variable)){
                    variableFactors.add(factor);
                }
            }
            // 6.1.1 remove factors that contain the variable from the factors list
            factors.removeAll(variableFactors);
            // 6.1.2 check if the factors list contains only one factor with one variable
            if(removeSingleVariableFactors(variableFactors, queryVariable)){
                continue;
            }
            // 6.2 sort the list by factors size (if the size is the same sort by ASCII order of the variable name)
            Collections.sort(variableFactors);
            // 6.3 join the factors by order
            Factor joinedFactor = join(variableFactors, variable, this.mulCounter);
            // 6.4 eliminate the variable
            joinedFactor.eliminate(variable, this.sumCounter);
            // 6.5 add the new factor to the factors list
            factors.add(joinedFactor);
        }
        // 7. Join the remaining factors (it should be only one factors containing the query variable)
        Factor resultFactor = join(factors, queryVariable, this.mulCounter);
        // 8. Normalize the resulting factor
        resultFactor.normalize(this.sumCounter);
        // 9. Get the probability of the query variable
        // Convert the query value to List<String> and get the value by the key
        double prob = resultFactor.getCpt().get(Collections.singletonList(queryValue));
        // 10. Return the result
        String ans = String.format("%.5f", prob) + "," + this.sumCounter + "," + this.mulCounter;
        System.out.println("Answer: " + ans);

        return ans;
    }

    private boolean removeSingleVariableFactors(List<Factor> variableFactors, String queryVariable) {
        for (Factor factor : variableFactors) {
            // Check if the factor contains only one variable and it is not the query variable
            if (factor.getVariables().size() != 1 || factor.getVariables().contains(queryVariable)) {
                return false;
            }
        }
        return true;
    }
    private List<String> getHidden(Set<NetNode> relevantNodes, List<String> eliminationOrder) {
        List<String> hidden = new ArrayList<>();
        for(String node : eliminationOrder){
            if(!relevantNodes.contains(network.getNode(node))){
                hidden.add(node);
            }
        }
        return hidden;
    }
    // function that receives String and return all the ancestors of the node
    private Set<NetNode> getRelevantNodes(String query, Map<String, String> evidence, List<String> eliminationOrder) {
           Set<NetNode> relevantNodes = new HashSet<>();
            // Add the query node and its ancestors
            String queryVariable = query.split("=")[0];
            relevantNodes.add(network.getNode(queryVariable));
            relevantNodes.addAll(network.getNode(queryVariable).getAncestors());
            // Add the evidence nodes and their ancestors
            for (String ev : evidence.keySet()) {
                relevantNodes.add(network.getNode(ev));
                relevantNodes.addAll(network.getNode(ev).getAncestors());
            }
            List<String> hidden = getHidden(relevantNodes, eliminationOrder);
            for(String node : hidden){
                NetNode hiddenNode = network.getNode(node);
                boolean leaf = hiddenNode.isLeaf();
                boolean isEvidence = evidence.containsKey(node);
                boolean isQuery = queryVariable.equals(node);
                if(hiddenNode.isLeaf() &&
                  (!evidence.containsKey(node)) &&
                  (!queryVariable.equals(node))) {
                    hidden.remove(node);
                }
            }
            BayesBall bayesBall = new BayesBall(network);
//            for(String target : eliminationOrder){
            for(String target : hidden){
//                relevantNodes.addAll(bayesBall.getRelevantNodes(queryVariable, target, evidence));
                boolean result = bayesBall.areIndependent(queryVariable, target, evidence);
                boolean result2 = bayesBall.areIndependent(target, queryVariable, evidence); // debug
                if(!result){
                    relevantNodes.add(network.getNode(target));
                }
            }
            return relevantNodes;
        }


    public List<Factor> testRemoveEvidenceVariable(String query, Map<String, String> evidence, List<String> eliminationOrder) {
        // Implement the Variable Elimination Algorithm
        // queryVariable = "Q=q"
        //1.  Parse the query
        String[] queryParts = query.split("=");
        String queryVariable = queryParts[0];
        String queryValue = queryParts[1];
        // 2. get the relevant nodes for the query
        Set<NetNode> relevantNodes = getRelevantNodes(queryVariable, evidence, eliminationOrder);
        // 3. remove the irrelevant nodes from eliminationOrder list
        for(String node : eliminationOrder){
            if(!relevantNodes.contains(network.getNode(node))){
                eliminationOrder.remove(node);
            }
        }
        // 4. Create a list of factors for each node in the network
        List<Factor> factors = new ArrayList<>();
        for(NetNode node : relevantNodes){
            factors.add(node.getCPT().toFactor());
        }

        System.out.println("Factors before: ");
        for(Factor factor : factors){
            System.out.println(factor.toString());
        }
        // 5. Initialize the factors with the evidence
        for(Map.Entry<String, String> entry : evidence.entrySet()){
            for(Factor factor : factors){
                if(factor.getVariables().contains(entry.getKey())){
                    factor.removeEvidenceVariable(entry.getKey(), entry.getValue());
                }
            }
        }
        System.out.println("Factors after: ");
        for(Factor factor : factors){
            System.out.println(factor.toString());
        }
        // 3. Eliminate the variables in the elimination order
        // 4. Multiply the remaining factors
        // 5. Normalize the resulting factor
        // 6. Return the probability of the query variable
        return factors;
    }
//    public Factor testJoinOperation(String varName, List<Factor> factors) {
//
//    }
    }

