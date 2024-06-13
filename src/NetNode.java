import java.util.*;

public class NetNode {
    private String name;
    private List<String> states;
    private List<String> outcomes;
    private List<NetNode> parents;
    private List<NetNode> children;

    private CPT cpt;
    private boolean fromParent;
    private boolean fromChild;

    public NetNode(String name) {
        this.name = name;
        this.states = new ArrayList<>();
        this.outcomes = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.cpt = new CPT();
        this.fromParent = false;
        this.fromChild = false;
       }
    public boolean isFromChild() {
        return fromChild;
    }

    public void setFromChild() {
        if(!fromChild) {
            this.fromChild = true;
        }
    }

    public boolean isFromParent() {
        return fromParent;
    }

    public void setFromParent() {
        if(!fromParent) {
            this.fromParent = true;
        }
    }

    public void clearFlags() {
        this.fromParent = false;
        this.fromChild = false;
    }

    public String getName() {
        return name;
    }

    public void setOutcomes(List<String> outcomes) {
        this.outcomes = outcomes;
    }

    public void printNode() {
        System.out.println("Node: " + name);
        System.out.println("Outcomes: " + outcomes);
        System.out.println("Parents: " + parents);
        System.out.println("Children: " + children);
        System.out.println("CPT: " + cpt);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Node: ").append(name).append("\n");
        str.append("Outcomes: ").append(outcomes).append("\n");
        str.append("Parents: ");
        for (NetNode parent : parents) {
            str.append(parent.getName()).append(" ");
        }
        str.append("\nChildren: ");
        for (NetNode child : children) {
            str.append(child.getName()).append(" ");
        }
        str.append("\nCPT: ").append(cpt).append("\n");
        return str.toString();
    }

    public List<String> getStates() {
        return states;
    }

    public List<NetNode> getParents() {
        return parents;
    }

    public List<NetNode> getChildren() {
        return children;
    }

    public void addParent(NetNode parent) {
        parents.add(parent);
    }

    public void addChild(NetNode child) {
        children.add(child);
    }

    public void setCPT(CPT cpt) {
        this.cpt = cpt;
    }

    public CPT getCPT() {
        return cpt;
    }

    public List<String> getOutcomes() {
        return this.outcomes;
    }

    // Recursive function to get all ancestors of a node
    public Set<NetNode> getAncestors() {
        Set<NetNode> ancestors = new HashSet<>();
        getAncestorsHelper(this, ancestors);
        return ancestors;
    }
    // Helper function for getAncestors
    private void getAncestorsHelper(NetNode node, Set<NetNode> ancestors) {
        for (NetNode parent : node.getParents()) {
            if (ancestors.add(parent)) {
                getAncestorsHelper(parent, ancestors);
            }
        }
    }
}