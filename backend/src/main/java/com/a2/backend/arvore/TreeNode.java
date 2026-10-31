package com.a2.backend.arvore;

import java.util.ArrayList;
import java.util.List;

/**
 * Nó de uma árvore genérica: cada nó guarda um dado do tipo T, uma referência
 * ao pai e uma lista com um número qualquer de filhos.
 */
public class TreeNode<T> {

    private final T data;
    private TreeNode<T> parent;
    private final List<TreeNode<T>> children = new ArrayList<>();

    public TreeNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public TreeNode<T> addChild(TreeNode<T> child) {
        child.parent = this;
        children.add(child);
        return child;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    /** Nós em pré-ordem (pai antes dos filhos). */
    public List<TreeNode<T>> preOrder() {
        List<TreeNode<T>> result = new ArrayList<>();
        collectPreOrder(this, result);
        return result;
    }

    /** Nós em pós-ordem (filhos antes do pai). Usado pela implosão. */
    public List<TreeNode<T>> postOrder() {
        List<TreeNode<T>> result = new ArrayList<>();
        collectPostOrder(this, result);
        return result;
    }

    private static <T> void collectPreOrder(TreeNode<T> node, List<TreeNode<T>> out) {
        out.add(node);
        for (TreeNode<T> child : node.children) {
            collectPreOrder(child, out);
        }
    }

    private static <T> void collectPostOrder(TreeNode<T> node, List<TreeNode<T>> out) {
        for (TreeNode<T> child : node.children) {
            collectPostOrder(child, out);
        }
        out.add(node);
    }
}
