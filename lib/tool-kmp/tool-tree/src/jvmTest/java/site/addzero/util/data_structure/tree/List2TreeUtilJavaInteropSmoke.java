package site.addzero.util.data_structure.tree;

import java.util.ArrayList;
import java.util.List;

public final class List2TreeUtilJavaInteropSmoke {

    private List2TreeUtilJavaInteropSmoke() {
    }

    public static List<JavaNode> sampleNodes() {
        List<JavaNode> nodes = new ArrayList<>();
        nodes.add(new JavaNode(1, null));
        nodes.add(new JavaNode(2, 1));
        nodes.add(new JavaNode(3, 1));
        return nodes;
    }

    public static List<JavaNode> buildTree(List<JavaNode> source) {
        return List2TreeJavaUtil.list2Tree(
                source,
                JavaNode::getId,
                JavaNode::getParentId,
                JavaNode::getChildren,
                JavaNode::setChildren
        );
    }

    public static List<JavaNode> flattenTree(List<JavaNode> tree) {
        return List2TreeJavaUtil.tree2List(
                tree,
                JavaNode::getChildren,
                JavaNode::setChildren
        );
    }

    public static final class JavaNode {
        private final Integer id;
        private final Integer parentId;
        private List<JavaNode> children = new ArrayList<>();

        public JavaNode(Integer id, Integer parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        public Integer getId() {
            return id;
        }

        public Integer getParentId() {
            return parentId;
        }

        public List<JavaNode> getChildren() {
            return children;
        }

        public void setChildren(List<JavaNode> children) {
            this.children = children;
        }
    }
}
