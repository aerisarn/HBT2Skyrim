package org.tes.hkx.tree;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class HKXTreeRenderer implements TreeCellRenderer {

	JLabel label = new JLabel(" ");
	JPanel renderer = new JPanel();

	TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	// Constructor: just remember the renderer
	public HKXTreeRenderer(TreeCellRenderer renderer) {
		this.defaultRenderer = renderer;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		// Use the wrapped renderer object to do the real work
		return defaultRenderer.getTreeCellRendererComponent(tree, value.toString(), selected, expanded, leaf, row,
				hasFocus);
	}
}
