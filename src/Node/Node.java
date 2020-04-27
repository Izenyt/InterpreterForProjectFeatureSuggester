package Node;

import NodeType.NodeType;

public class Node {
    private final NodeType getNodeType;
    private final Node left;
    private final Node right;
    private final String getValue;

    public NodeType getNodeType() {
        return getNodeType;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public String getValue() {
        return getValue;
    }

    Node(NodeType nodeType, Node left, Node right, String value) {
        this.getNodeType = nodeType;
        this.left = left;
        this.right = right;
        this.getValue = value;
    }

    public static Node makeNode(NodeType nodetype, Node left, Node right) { return new Node(nodetype, left, right, ""); }
    public static Node makeNode(NodeType nodetype, Node left) { return new Node(nodetype, left, null, ""); }
    public static Node makeLeaf(NodeType nodetype, String value) { return new Node(nodetype, null, null, value); }
}