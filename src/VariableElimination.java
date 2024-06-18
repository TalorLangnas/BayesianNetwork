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
//    private boolean checkIfQvDependentOnHiddenVars(List<Factor> factors, )
    // Check if there is a factor that contains all the relevant variables and extract the result
//    private String checkIfExistResultFactor(List<Factor> factors, String queryValue, String queryVariable, Map<String, String> evidenceVars, List<String> hiddenVars){
//        for(Factor factor : factors){
//            // case: check if the q.v is not dependent on the hidden variables
//            // we can return the answer from the factor that contains the query variable
//            boolean bool = factor.checkIfFactorDependentOnHiddenVars(queryVariable, evidenceVars, hiddenVars);
////            boolean containsResult = factor.checkIfExistResultFactor(queryVariable, evidenceVars, hiddenVars, this.sumCounter);
//            if(factor.checkIfExistResultFactor(queryVariable, evidenceVars, hiddenVars, this.sumCounter)){
////            if(containsResult){
//                double prob = factor.getCpt().get(Collections.singletonList(queryValue));
////                String ans = String.format("%.5f", prob) + "," + this.sumCounter + "," + this.mulCounter;
//                return String.format("%.5f", prob) + "," + this.sumCounter + "," + this.mulCounter;
//            }
//        }
//        return null;
//    }
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

    private boolean checkRelation(NetNode node, Set<NetNode> otherNodes){
        for(NetNode child : node.getChildren()){
            if(otherNodes.contains(child)){
                return true;
            }
        }
        return false;
    }
    // case 2: we left with query variable only
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
    // create boolean function that checks if the node is leaf considering the relevant nodes
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
//    public String eliminate(String query, Map<String, String> evidence, List<String> eliminationOrder) {
    public String eliminate(String queryVariable, String queryValue, Map<String, String> evidence, List<String> eliminationOrder) {
        // Variable Elimination Algorithm

        Set<NetNode> evidenceNodes = new HashSet<>(); // Create a set of evidence nodes
        for(Map.Entry<String, String> entry : evidence.entrySet()){
            evidenceNodes.add(network.getNode(entry.getKey()));
        }

        // 1. get the relevant nodes for the query
        Set<NetNode> relevantNodes = getRelevantNodes(queryVariable, evidence, eliminationOrder);
//        Set<NetNode> relevantNodes2 = getRelevantNodes2(queryVariable, evidence, eliminationOrder);

//        removeLeafs(relevantNodes, evidenceNodes, network.getNode(queryVariable));
//        removeLeafs(relevantNodes2, evidenceNodes, network.getNode(queryVariable)); // debug
//
//        removeIndependentNodes(relevantNodes2, evidenceNodes, queryVariable);
        // 2. remove the irrelevant nodes from eliminationOrder list
        // Casting eliminationOrder to ArrayList to avoid ConcurrentModificationException
        ArrayList<String> eliminationOrderCopy = new ArrayList<>(eliminationOrder);
        for(String node : eliminationOrder){
            if(!relevantNodes.contains(network.getNode(node))){
//               boolean removed = eliminationOrder.remove(node);
                eliminationOrderCopy.remove(node);
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
        eliminationOrder = eliminationOrderCopy;
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
        // case 2: the factor contains the query variable and all the evidence variables with no other hidden variables
        String checkQueryAndEvidenceResult = checkQueryAndEvidence(factors, queryValue, queryVariable, evidence, eliminationOrder);
        if(checkQueryAndEvidenceResult != null){
            return checkQueryAndEvidenceResult;
        }
//        // case 3: Checks if we have factor contains all the relevant variables
//        String checkResult = checkIfExistResultFactor(factors, queryValue, queryVariable, evidence, eliminationOrder);
//        if(checkResult != null){
//            return checkResult;
//        }
        // 4. Initialize the factors with the relevant entries (with no the evidence variables)
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
        // If factor contains irrelevant variable, remove the factor
//        factorsToRemove.clear();

        checkFactorsVariables(factors, eliminationOrder, queryVariable);
        // 5. If we have hidden variables Eliminate them by the elimination order
        for(String variable : eliminationOrder){
            if(variable.equals(queryVariable)){
                // Skip the query variable, we don't need to eliminate it
                continue;
            }
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
        // add while loope to join the remaining factors
        Factor resultFactor = join(this.network, factors, queryVariable, this.mulCounter);
        // 7. Normalize the resulting factor
        resultFactor.normalize(this.sumCounter);
        // 8. Get the probability of the query variable
        // Convert the query value to List<String> and get the value by the key
        Double prob = resultFactor.getCpt().get(Collections.singletonList(queryValue));
        if(prob == null){
            System.out.println("input Query Value: " + queryValue);
            System.out.println("Query value outcomes: " + network.getNode(queryVariable).getOutcomes());
            System.out.println("Error: The query value is not found in the resulting factor");
            prob = 0.000000;
        }
        // 9. Return the result
        String ans = String.format("%.5f", prob) + "," + this.sumCounter + "," + this.mulCounter;
        System.out.println("Answer: " + ans);

        return ans;
    }

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

//        BayesBall bayesBall = new BayesBall(network);
//        Set<NetNode> relevantNodes = new HashSet<>();
//        // Add the query node and its ancestors
//        relevantNodes.add(network.getNode(queryVariable));
//        relevantNodes.addAll(network.getNode(queryVariable).getAncestors());
//        // Add the evidence nodes and their ancestors
//        for (String ev : evidence.keySet()) {
//            relevantNodes.add(network.getNode(ev));
//            relevantNodes.addAll(network.getNode(ev).getAncestors());
//        }
//        List<String> hidden = getHidden(relevantNodes, eliminationOrder);
//        List<String> nodesToRemove = new ArrayList<>();
//            for(String node : hidden){
//                NetNode hiddenNode = network.getNode(node);
//                boolean leaf = hiddenNode.isLeaf();
//                boolean isEvidence = evidence.containsKey(node);
//                boolean isQuery = queryVariable.equals(node);
//                if(hiddenNode.isLeaf() &&
//                  (!evidence.containsKey(node)) &&
//                  (!queryVariable.equals(node))) {
//                    nodesToRemove.add(node);
////                    hidden.remove(node);
//                }
//            }
//            hidden.removeAll(nodesToRemove);
//
////            for(String target : eliminationOrder){
//            for(String target : hidden){
////                relevantNodes.addAll(bayesBall.getRelevantNodes(queryVariable, target, evidence));
//                boolean result = bayesBall.areIndependent(queryVariable, target, evidence);
//                if(!result){
//                    relevantNodes.add(network.getNode(target));
//                }
//            }
//            return relevantNodes;
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
//        List<String> hidden = getHidden(relevantNodes, eliminationOrder);
//        List<String> nodesToRemove = new ArrayList<>();
        for(String node : eliminationOrder){
            if(!bayesBall.areIndependent(queryVariable, node, evidence)){
                relevantNodes.add(network.getNode(node));
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

