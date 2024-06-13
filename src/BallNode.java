public class BallNode extends NetNode {

    private boolean fromParent;
    private boolean fromChild;

    public BallNode(String nodeName) {
        super(nodeName);
        fromParent = false;
        fromChild = false;
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
}
