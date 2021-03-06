package nodebox.client;

import nodebox.node.Node;

import javax.swing.*;
import java.awt.*;

public abstract class Pane extends JPanel implements DocumentFocusListener {

    private NodeBoxDocument document;

    public Pane() {
    }

    public Pane(NodeBoxDocument document) {
        setDocument(document);
    }

    public void setDocument(NodeBoxDocument document) {
        NodeBoxDocument oldDocument = this.document;
        if (oldDocument != null)
            oldDocument.removeDocumentFocusListener(this);
        this.document = document;
        if (document != null)
            document.addDocumentFocusListener(this);
    }

    public NodeBoxDocument getDocument() {
        return document;
    }

    public abstract Pane clone();

    public abstract String getPaneName();

    public void currentNodeChanged(Node activeNetwork) {
    }

    public void focusedNodeChanged(Node activeNode) {
    }

    /**
     * Splits the pane into two vertically aligned panes. This pane will be relocated as the top pane.
     * The bottom pane will be a clone of this pane.
     */
    public void splitTopBottom() {
        split(NSplitter.Orientation.VERTICAL);
    }

    /**
     * Splits the pane into two horizontally aligned panes. This pane will be relocated as the left pane.
     * The right pane will be a clone of this pane.
     */
    public void splitLeftRight() {
        split(NSplitter.Orientation.HORIZONTAL);
    }

    private void split(NSplitter.Orientation orientation) {
        Container parent = getParent();
        if (parent instanceof PaneSplitter) {
            PaneSplitter parentSplit = (PaneSplitter) parent;
            boolean first = parentSplit.getFirstComponent() == this;
            if (first) {
                parentSplit.setFirstComponent(null);
            } else {
                parentSplit.setSecondComponent(null);
            }
            PaneSplitter split = new PaneSplitter(orientation, this, this.clone());
            if (first) {
                parentSplit.setFirstComponent(split);
            } else {
                parentSplit.setSecondComponent(split);
            }
            parentSplit.validate();
        } else {
            parent.remove(this);
            PaneSplitter split = new PaneSplitter(orientation, this, this.clone());
            parent.add(split);
            parent.validate();
        }
    }

    public void close() {
        Container parent = getParent();
        if (!(parent instanceof PaneSplitter)) return;
        PaneSplitter split = (PaneSplitter) parent;
        JComponent firstComponent = split.getFirstComponent();
        JComponent secondComponent = split.getSecondComponent();
        JComponent remainingComponent = firstComponent == this ? secondComponent : firstComponent;
        split.setFirstComponent(null);
        split.setSecondComponent(null);
        Container grandParent = parent.getParent();
        if (grandParent instanceof PaneSplitter) {
            PaneSplitter grandSplit = (PaneSplitter) grandParent;
            // Remove the split pane.
            if (split == grandSplit.getFirstComponent()) {
                grandSplit.setFirstComponent(remainingComponent);
            } else {
                grandSplit.setSecondComponent(remainingComponent);
            }
        } else {
            grandParent.remove(parent);
            grandParent.add(remainingComponent);
        }
        grandParent.validate();
    }

    public void changePaneType(Class paneType) {
        if (!Pane.class.isAssignableFrom(paneType)) return;
        Pane newPane;
        try {
            newPane = (Pane) paneType.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        newPane.setDocument(getDocument());
        Container parent = getParent();
        if (parent instanceof PaneSplitter) {
            PaneSplitter parentSplit = (PaneSplitter) parent;
            boolean first = parentSplit.getFirstComponent() == this;
            if (first) {
                parentSplit.setFirstComponent(newPane);
            } else {
                parentSplit.setSecondComponent(newPane);
            }
        } else {
            Dimension d = getSize();
            parent.remove(this);
            parent.add(newPane);
            newPane.setSize(d);
        }
        parent.validate();
    }

}
