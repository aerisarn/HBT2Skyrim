package org.tes.hkx.tree;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.tes.hkx.lib.HkobjectType;
import org.tes.hkx.model.IHkContainer;
import org.tes.hkx.model.IHkVisitable;

public class HKXTree extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5212345459874803591L;

	public HKXTree(HkobjectType c) {
		super(new HKXTreeModel(c));
		setCellRenderer(new HKXTreeRenderer(getCellRenderer()));
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	static class HKXTreeModel implements TreeModel {
		HkobjectType root; // The root object of the tree

		// Constructor: just remember the root object
		public HKXTreeModel(HkobjectType root) {
			this.root = root;
		}

		// Return the root of the tree
		public Object getRoot() {
			return root;
		}

		// Is this node a leaf? (Leaf nodes are displayed differently by JTree)
		// Any node that isn't a container is a leaf, since they cannot have
		// children. We also define containers with no children as leaves.
		public boolean isLeaf(Object node) {
			if (!(node instanceof IHkContainer))
				return true;
			IHkContainer c = (IHkContainer) node;
			return c.objects().size() == 0;
		}

		// How many children does this node have?
		public int getChildCount(Object node) {
			if (node instanceof IHkContainer) {
				IHkContainer c = (IHkContainer) node;
				Set<IHkVisitable> uniqueChildrens = new LinkedHashSet<>();
				uniqueChildrens.addAll(c.objects());
				return uniqueChildrens.size();
			}
			return 0;
		}

		// Return the specified child of a parent node.
		public Object getChild(Object parent, int index) {
			if (parent instanceof IHkContainer) {
				IHkContainer c = (IHkContainer) parent;
				if (c.objects() == null || index < 0 || index >= c.objects().size())
					return null;
				Set<IHkVisitable> uniqueChildrens = new LinkedHashSet<>();
				uniqueChildrens.addAll(c.objects());
				int i = 0;
				Iterator<IHkVisitable> ii = uniqueChildrens.iterator();
				while (ii.hasNext()) {
					if (i == index)
						return ii.next();
					ii.next();
					i++;
				}
				return null;
			}
			return null;
		}

		// Return the index of the child node in the parent node
		public int getIndexOfChild(Object parent, Object child) {
			if (!(parent instanceof IHkContainer))
				return -1;
			IHkContainer c = (IHkContainer) parent;
			if (c.objects() == null)
				return -1;
			Set<IHkVisitable> uniqueChildrens = new LinkedHashSet<>();
			uniqueChildrens.addAll(c.objects());
			Iterator<IHkVisitable> ii = uniqueChildrens.iterator();
			int index = 0;
			while (ii.hasNext()) {
				IHkVisitable v = ii.next();
				if (v.equals(child)) {
					return index;
				}
				index++;
			}
			return -1;
		}

		// This method is only required for editable trees, so it is not
		// implemented here.
		public void valueForPathChanged(TreePath path, Object newvalue) {
		}

		// This TreeModel never fires any events (since it is not editable)
		// so event listener registration methods are left unimplemented
		public void addTreeModelListener(TreeModelListener l) {
		}

		public void removeTreeModelListener(TreeModelListener l) {
		}
	}

}
