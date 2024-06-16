import java.io.*;
import java.util.*;

public class BayesBall {
    BayesianNetwork network;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    private void addParentsToQueue(NetNode curr, Queue<String> queue){
        for (NetNode parent : curr.getParents()) {
            if(!curr.getVisitedParents(curr.getParents().indexOf(parent))) {
                curr.setVisitedParents(curr.getParents().indexOf(parent));
                parent.setFromChild();
                queue.add(parent.getName());
            }
        }
    }

    private void addChildrenToQueue(NetNode curr, Queue<String> queue){
        for (NetNode child : curr.getChildren()) {
            if (!curr.getVisitedChildren(curr.getChildren().indexOf(child))){
                curr.setVisitedChildren(curr.getChildren().indexOf(child));
                child.setFromParent();
                queue.add(child.getName());
            }
        }
    }

    public boolean areIndependent(String source, String target, Map<String, String> evidence) {
//        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        // Adding the source node to the queue
        queue.add(source);

        while(! queue.isEmpty()) {
            String currName = queue.poll();
//            visited.add(currName);

            // Check if we reached the target node
            if (currName.equals(target)) {
                // We reached the target node => the nodes are dependent
                network.resetFlags(); // Rest the boolean flags
                return false;
            }
            NetNode curr = network.getNode(currName);
            // if this is the source node, then we can go to any of its children and parents
            if(currName.equals(source)) {
                addChildrenToQueue(curr, queue);
                addParentsToQueue(curr, queue);
            }

            // If the ball is received from a parent and the node does not have evidence, the ball is
            // passed to all children of this node.

            // if we arrived to unobserved node from his parent, we can go only to his children
            if(curr.isFromParent() && ! evidence.containsKey(currName)) {
                addChildrenToQueue(curr, queue);
            }
            // If the ball is received from a child and the node does not have evidence, the ball is
            // bounced back to all the children of the node and passed to all parents of the node.
            if(curr.isFromChild() && ! evidence.containsKey(currName)) {
                addParentsToQueue(curr, queue);
                addChildrenToQueue(curr, queue);
            }
            // If the ball is received from a parent and the node has evidence, the ball is bounced
            // back to all parents of the node
            if(curr.isFromParent() && evidence.containsKey(currName)) {
                addParentsToQueue(curr, queue);
            }
            // If the ball is received from a child and the node has evidence, the ball is blocked (it
            //is not passed to any node).
            if(curr.isFromChild() && evidence.containsKey(currName)) {
                continue;
            }
            curr.clearFlags();
        }
        // We did not reach the target node => the nodes are Conditional Independent
        network.resetFlags(); // Rest the boolean flags
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


