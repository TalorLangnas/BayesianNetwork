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

    public Factor join(BayesianNetwork network, List<Factor> factors, String varName, AtomicInteger mulCounter){
        while(factors.size() > 1){
            // Extract the first two factors from the list
            Factor f1 = factors.remove(0);
            Factor f2 = factors.remove(0);
            // Join the two factors on the specified variable
            Factor joinedFactor = f1.join(network, f2, varName, mulCounter);
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

    // Check if we can extract the result from the factor that contains the query variable only
    public String checkQueryAndEvidence(List<Factor> factors, String queryValue, String queryVariable,Map<String, String> evidenceVars, List<String> hidden){
        Set<NetNode> evidenceNodes = new HashSet<>();
        for(Map.Entry<String, String> entry : evidenceVars.entrySet()){
            evidenceNodes.add(network.getNode(entry.getKey()));
        }
        if(checkRelation(network.getNode(queryVariable), evidenceNodes)){
            return null;
        }
        for(Factor factor : factors){
            if(factor.checkQueryAndEvidence(queryVariable, evidenceVars, hidden)){
                for(Map.Entry<String, String> entry : evidenceVars.entrySet()){
                    factor.removeEvidenceVariable(entry.getKey(), entry.getValue());
                }
                double prob = factor.getCpt().get(Collections.singletonList(queryValue));
                return String.format("%.5f", prob) + "," + this.sumCounter + "," + this.mulCounter;
            }
        }
        return null;
    }

    // Checks if there is an arc between node to any NetNode in otherNodes
    private boolean checkRelation(NetNode node, Set<NetNode> otherNodes){
        for(NetNode child : node.getChildren()){
            if(otherNodes.contains(child)){
                return true;
            }
        }
        return false;
    }
    // Checks if there is a factor with query variable only, extract the result if it is possible
    private String checkOnlyQueryLeft(List<Factor> factors, Set<NetNode>evidenceNodes, String queryVariable, String queryValue){
        // If dependency relation is existed between the query variable and evidence variables
        // we can not extract the result from the factor that contains the query variable only
        if(checkRelation(network.getNode(queryVariable), evidenceNodes)){
            return null;
        }
        for(Factor factor : factors){
            if(factor.getVariables().size() == 1 && factor.getVariables().contains(queryVariable)){
                double prob = factor.getCpt().get(Collections.singletonList(queryValue));
                return String.format("%.5f", prob) + "," + this.sumCounter + "," + this.mulCounter;
            }
        }
        return null;
    }
    private void removeLeafs(Set<NetNode> relevantNodes, Set<NetNode> evidenceNodes, NetNode queryNode){
        int tmpSize = 0;
        while(tmpSize != relevantNodes.size()){
            tmpSize = relevantNodes.size();
            AsstRemoveLeafs(relevantNodes, evidenceNodes, queryNode);
        }
    }
    // Asst function to remove leaf nodes from the relevant nodes
    private void AsstRemoveLeafs(Set<NetNode> relevantNodes, Set<NetNode> evidenceNodes, NetNode queryNode){
        // Create a new HashSet to avoid ConcurrentModificationException
        Set<NetNode> nodesToRemove = new HashSet<>();

        // Iterate through each node in relevantNodes
        for (NetNode node : relevantNodes) {
            if(node.equals(queryNode)){
                continue;
            }
            if(evidenceNodes.contains(node)){
                continue;
            }
            boolean hasRelevantChild = false;
            // Check if at least one child of the node is in relevantNodes
            for (NetNode child : node.getChildren()) {
                if (relevantNodes.contains(child)) {
                    hasRelevantChild = true;
                    break;
                }
            }
            // If no child of the node is in relevantNodes, mark the node for removal
            if (!hasRelevantChild) {
                nodesToRemove.add(node);
            }
        }

        // Remove the nodes that were marked for removal
        relevantNodes.removeAll(nodesToRemove);
    }

    // Check if the factors contain hidden variables
    private boolean containsHidden(List<Factor> factors, List<String> hidden){
        for(Factor factor : factors){
           for(String variable : factor.getVariables()){
                if(hidden.contains(variable)){
                     return true;
                }
           }
        }
        return false;
    }

    /**
     * The process involves eliminating irrelevant nodes from the list
     * checking to ensure that the query and evidence variables are not included in the list.
     * Appends, any hidden variables located in the relevantNodes that are not
     * present in the eliminationOrder list.
     * @param eliminationOrder
     * @param relevantNodes
     * @param queryVariable
     * @param evidence
     */
    private List<String> preprocessEliminationOrder(List<String> eliminationOrder, Set<NetNode> relevantNodes, String queryVariable, Map<String, String> evidence){
        ArrayList<String> eliminationOrderCopy = new ArrayList<>(eliminationOrder);
        for(String node : eliminationOrder){
            if(!relevantNodes.contains(network.getNode(node))){
//               boolean removed = eliminationOrder.remove(node);
                eliminationOrderCopy.remove(node);
            }
        }
        // Add relevant hidden variables
        for(Map.Entry<String, NetNode> entry : this.network.getNodes().entrySet()){
            if(entry.getKey().equals(queryVariable) || evidence.containsKey(entry.getKey())) {
                continue;
            }
            if(relevantNodes.contains(entry.getValue()) && !eliminationOrderCopy.contains(entry.getKey())){
                eliminationOrderCopy.add(entry.getKey());
            }
        }

        // remove evidence variables from eliminationOrder
        for(String node : eliminationOrder){
            if(evidence.containsKey(node)){
                eliminationOrderCopy.remove(node);
            }
        }
        // remove query variable from eliminationOrder
        if(eliminationOrderCopy.contains(queryVariable)){
            eliminationOrderCopy.remove(queryVariable);
        }
       return eliminationOrderCopy;
    }
    public String eliminate(String queryVariable, String queryValue, Map<String, String> evidence, List<String> eliminationOrder) {
        // Variable Elimination Algorithm
        Set<NetNode> evidenceNodes = new HashSet<>(); // Create a set of evidence nodes
        for(Map.Entry<String, String> entry : evidence.entrySet()){
            evidenceNodes.add(network.getNode(entry.getKey()));
        }
        // 1. get the relevant nodes for the query
        Set<NetNode> relevantNodes = getRelevantNodes(queryVariable, evidence, eliminationOrder);
        // 2. remove the irrelevant nodes from eliminationOrder list
        eliminationOrder = preprocessEliminationOrder(eliminationOrder, relevantNodes, queryVariable, evidence);
        // 3. Create a list of factors for each node in the network
        List<Factor> factors = new ArrayList<>();
        for(NetNode node : relevantNodes){
            factors.add(node.getCPT().toFactor());
        }
        // case 1: Checks if the relevantNodes contain only the query variable
        // check if factors do not contain any hidden variables
        if(!containsHidden(factors, eliminationOrder)){
            String checkOnlyQueryLeftResult = checkOnlyQueryLeft(factors, evidenceNodes, queryVariable, queryValue);
            if(checkOnlyQueryLeftResult != null){
                return checkOnlyQueryLeftResult;
            }
        }

        // Checks if exist factor that contains the query result,
        // case: the factor contains the query variable and all the evidence variables with no other hidden variables
        String checkQueryAndEvidenceResult = checkQueryAndEvidence(factors, queryValue, queryVariable, evidence, eliminationOrder);
        if(checkQueryAndEvidenceResult != null){
            return checkQueryAndEvidenceResult;
        }
        // 4. Initialize the factors with the relevant entries (with no the evidence variables)
        List<Factor>  factorsToRemove = new ArrayList<>();
        for(Factor factor : factors){
            for(Map.Entry<String, String> entry : evidence.entrySet()){
                if(factor.getVariables().contains(entry.getKey())){
                    factor.removeEvidenceVariable(entry.getKey(), entry.getValue());
                    if(factor.getVariables().size() == 0){
                        factorsToRemove.add(factor);
                    }
                }
            }
        }
        // Remove factors with no variables
        factors.removeAll(factorsToRemove);

        checkFactorsVariables(factors, eliminationOrder, queryVariable);
        // 5. If we have hidden variables Eliminate them by the elimination order
        for(String variable : eliminationOrder){
            // 5.1 create temp list with the relevant factors
            List<Factor> variableFactors = new ArrayList<>();
            for(Factor factor : factors){
                if(factor.getVariables().contains(variable)){
                    variableFactors.add(factor);
                }
            }
            // 5.1.1 remove factors that contain the variable from the factors list
            factors.removeAll(variableFactors);
            // 5.1.2 check if the factors list contains only one factor with one variable
            if(removeSingleVariableFactors(variableFactors, queryVariable)){
                continue;
            }
            // 5.2 sort the list by factors size (if the size is the same sort by ASCII order of the variable name)
            Collections.sort(variableFactors);
            // 5.3 join the factors by order
            Factor joinedFactor = join(this.network, variableFactors, variable, this.mulCounter);
            // 5.4 eliminate the variable
            joinedFactor.eliminate(variable, this.sumCounter);
            // 5.5 add the new factor to the factors list
            factors.add(joinedFactor);
        }
        // 6. Join the remaining factors (it should be only one factors containing the query variable)
        Factor resultFactor = join(this.network, factors, queryVariable, this.mulCounter);
        // 7. Normalize the resulting factor
        resultFactor.normalize(this.sumCounter);
        // 8. Get the probability of the query variable
        Double prob = resultFactor.getCpt().get(Collections.singletonList(queryValue));
        if(prob == null){
            System.out.println("input Query Value: " + queryValue);
            System.out.println("Query value outcomes: " + network.getNode(queryVariable).getOutcomes());
            System.out.println("Error: The query value is not found in the resulting factor");
            prob = 0.000000;
        }
        // 9. Return the result
        String ans = String.format("%.5f", prob) + "," + this.sumCounter + "," + this.mulCounter;
//        System.out.println("Answer: " + ans);

        return ans;
    }

    // Removes factors that contain variables not in the elimination order
    private void checkFactorsVariables(List<Factor> factors, List<String> eliminationOrder, String queryVariable){
        List<Factor> factorsToRemove = new ArrayList<>();

        // Iterate through each factor
        for (Factor factor : factors) {
            boolean shouldRemove = false;

            // Check each variable in the factor
            for (String variable : factor.getVariables()) {
                if (!variable.equals(queryVariable) && !eliminationOrder.contains(variable)) {
                    shouldRemove = true;
                    break;
                }
            }

            // If any variable meets the condition, mark the factor for removal
            if (shouldRemove) {
                factorsToRemove.add(factor);
            }
        }

        // Remove the marked factors from the original list
        factors.removeAll(factorsToRemove);
    }


    private void removeIndependentNodes(Set<NetNode> relevantNodes, Set<NetNode> evidenceNodes, String queryVariable) {
        NetNode queryNode = network.getNode(queryVariable);
        BayesBall bayesBall = new BayesBall(network);
        Map<String,String> evidence = new HashMap<>();
        for(NetNode node : evidenceNodes){
            evidence.put(node.getName(), "");
        }

        List<NetNode> nodesToRemove = new ArrayList<>();
        for(NetNode node : relevantNodes){
            if(node.equals(queryNode)){
                continue;
            }
            if(evidenceNodes.contains(node)){
                continue;
            }
            if(bayesBall.areIndependent(queryVariable, node.getName(), evidence)){
                nodesToRemove.add(node);
            }
        }
        relevantNodes.removeAll(nodesToRemove);
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
    // Function the hidden nodes which are not already in the relevant nodes list
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
    private Set<NetNode> getRelevantNodes(String queryVariable, Map<String, String> evidence, List<String> eliminationOrder) {
        Set<NetNode> evidenceNodes = new HashSet<>();
        for(Map.Entry<String, String> entry : evidence.entrySet()){
            evidenceNodes.add(network.getNode(entry.getKey()));
        }
        Set<NetNode> relevantNodes = asstGetRelevantNodes(queryVariable, evidence, eliminationOrder);
        removeLeafs(relevantNodes, evidenceNodes, network.getNode(queryVariable));
        removeIndependentNodes(relevantNodes, evidenceNodes, queryVariable);

        return relevantNodes;

    }

    private Set<NetNode> asstGetRelevantNodes(String queryVariable, Map<String, String> evidence, List<String> eliminationOrder){
        BayesBall bayesBall = new BayesBall(network);
        Set<NetNode> relevantNodes = new HashSet<>();

        // Add the query node and its ancestors
        relevantNodes.add(network.getNode(queryVariable));
        relevantNodes.addAll(network.getNode(queryVariable).getAncestors());
        // Check conditional independence between the query node and the evidence nodes
        // Add the evidence nodes and their ancestors
        for (String ev : evidence.keySet()) {
            relevantNodes.add(network.getNode(ev));
            relevantNodes.addAll(network.getNode(ev).getAncestors());
        }
        for(String node : eliminationOrder){
            if(!bayesBall.areIndependent(queryVariable, node, evidence)){
                relevantNodes.add(network.getNode(node));
            }
        }
        return relevantNodes;
    }
}

