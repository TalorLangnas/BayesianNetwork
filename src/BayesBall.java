import java.io.*;
import java.util.*;

public class BayesBall {
    BayesianNetwork network;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    public boolean areIndependent(String source, String target, Map<String, String> evidence) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        // Adding the source node to the queue
        queue.add(source);

        while(! queue.isEmpty()) {
            String currName = queue.poll();

//            if (visited.contains(currName)) {
//                continue;
//            }
            visited.add(currName);

            // Check if we reached the target node
            if (currName.equals(target)) {
                // We reached the target node => the nodes are dependent
                return false;
            }
            NetNode curr = network.getNode(currName);
            // if this is the source node, then we can go to any of its children
            if(currName.equals(source)) {
                for (NetNode child : curr.getChildren()) {
                    child.setFromParent();
                    queue.add(child.getName());
                }
            }
            // maybe here is the place to enter the case that source node is leaf node
            // If the source node is a leaf node, then we go to his parent
            if(curr.isLeaf()) {
                for (NetNode parent : curr.getParents()) {
                    parent.setFromChild();
                    queue.add(parent.getName());
                }
            }

            // If the ball is received from a parent and the node does not have evidence, the ball is
            // passed to all children of this node.

            // if we arrived to unobserved node from his parent, we can go only to his children
            if(curr.isFromParent() && ! evidence.containsKey(currName)) {
                for (NetNode child : curr.getChildren()) {
                    child.setFromParent();
                    queue.add(child.getName());
                }
            }
            // If the ball is received from a child and the node does not have evidence, the ball is
            // bounced back to all the children of the node and passed to all parents of the node.
            if(curr.isFromChild() && ! evidence.containsKey(currName)) {
                for (NetNode parent : curr.getParents()) {
                    parent.setFromChild();
                    queue.add(parent.getName());
                }
                for (NetNode child : curr.getChildren()) {
                    child.setFromParent();
                    queue.add(child.getName());
                }
            }
            // If the ball is received from a parent and the node has evidence, the ball is bounced
            // back to all parents of the node
            if(curr.isFromParent() && evidence.containsKey(currName)) {
                for (NetNode parent : curr.getParents()) {
                    parent.setFromChild();
                    queue.add(parent.getName());
                }
            }
            // If the ball is received from a child and the node has evidence, the ball is blocked (it
            //is not passed to any node).
            if(curr.isFromChild() && evidence.containsKey(currName)) {
                continue;
            }
            curr.clearFlags();
        }
        // We did not reach the target node => the nodes are Conditional Independent
        return true;
    }

    public void processQueries(String inputFile, String outputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            String[] query = parts[0].split("-");
            String A = query[0];
            String B = query[1];

            Map<String, String> evidence = new HashMap<>();
            if (parts.length > 1) {
                String[] evidenceParts = parts[1].split(",");
                for (String ev : evidenceParts) {
                    String[] evParts = ev.split("=");
                    evidence.put(evParts[0], evParts[1]);
                }
            }

            boolean result = areIndependent(A, B, evidence);
            bw.write(result ? "yes" : "no");
            bw.newLine();
        }

        br.close();
        bw.close();
    }
}


