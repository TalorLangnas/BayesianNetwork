import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BayesianNetwork {

    private Map<String, NetNode> nodes;
    private Map<String, ArrayList<NetNode>> adjacencyList;

    public BayesianNetwork() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void printNetwork() {
        for (Map.Entry<String, NetNode> entry : nodes.entrySet()) {
            NetNode node = entry.getValue();
            System.out.println("Key: " + entry.getKey() + ", " + node.toString());
        }
    }

    public Map<String, NetNode> getNodes() {
        return nodes;
    }

    public void addNode(NetNode node) {
        nodes.put(node.getName(), node);
        ArrayList<NetNode> nodeList = new ArrayList<>();
        adjacencyList.put(node.getName(), nodeList);
    }

    /**
     * Add a node to the adjacency list of specified node
     * @param parentNodeName the name of the parent node
     * @param child the node to be added to the adjacency list
     */
    public void addNodeToAdjacencyList(String parentNodeName, NetNode child) {
        // Get the list associated with the key
        ArrayList<NetNode> nodeList = adjacencyList.get(parentNodeName);
        // Add the node to the list
        nodeList.add(child);
    }

    /**
     *Add the children List to the adjacency list of the specified node
     * @param nodeName name of the node
     * @param children is a ArrayList of BbNode objects which are the children of the node
     */
    public void addToAdjacencyList(String nodeName, ArrayList<NetNode> children) {
        adjacencyList.put(nodeName, children);
    }

    public NetNode getNode(String nodeName) {
        return nodes.get(nodeName);
    }

    public void resetFlags() {
        for (Map.Entry<String, NetNode> entry : nodes.entrySet()) {
            NetNode node = entry.getValue();
            node.resetFlags();
        }
    }
}
