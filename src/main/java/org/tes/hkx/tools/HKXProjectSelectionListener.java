package org.tes.hkx.tools;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tes.hkx.model.HkFile;
import org.tes.hkx.tree.HKXTree;

public class HKXProjectSelectionListener implements TreeSelectionListener {

	private JScrollPane scrollpane;
	private HKXTree rotree;
	private JFrame frame;
	private Map<String, HkFile> fileMap;
	private JTree ptree;
	private HKXTreeSelectionListener tlistener;

	public HKXProjectSelectionListener(JScrollPane scrollpane, HKXTree rotree, JFrame frame,
			Map<String, HkFile> fileMap, JTree ptree, HKXTreeSelectionListener tlistener) {
		this.scrollpane=scrollpane;
		this.rotree=rotree;
		this.frame=frame;
		this.fileMap=fileMap;
		this.ptree=ptree;
		this.tlistener=tlistener;

	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) ptree.getLastSelectedPathComponent();

		/* if nothing is selected */
		if (node == null)
			return;
		if (!node.isLeaf())
			return;

		/* retrieve the node that was selected */
		String nodeInfo = (String) node.getUserObject();
		System.out.println(nodeInfo);
		// scrollpane.remove(rotree);
		System.out.println(fileMap.get(nodeInfo).getRoot());
		rotree = new HKXTree(fileMap.get(nodeInfo).getRoot());
		tlistener.setTree(rotree);
		rotree.addTreeSelectionListener(tlistener);
		frame.remove(scrollpane);
		scrollpane = new JScrollPane(rotree);
		frame.add(scrollpane);
		frame.pack();

	}

}
