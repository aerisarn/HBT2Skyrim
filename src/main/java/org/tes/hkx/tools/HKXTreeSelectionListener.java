package org.tes.hkx.tools;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;

public class HKXTreeSelectionListener implements TreeSelectionListener {

	JTree tree;
	JFrame mainFrame = new JFrame("");

	public HKXTreeSelectionListener(JTree tree) {
		this.tree = tree;
	}
	
	public void setTree(JTree tree) {
		this.tree = tree;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Object node = tree.getLastSelectedPathComponent();

		/* if nothing is selected */
		if (node == null)
			return;

		System.out.println(node.toString());

/*		if (mainFrame != null) {
			mainFrame.setVisible(false);
			mainFrame.dispose();
		}*/

		String[] columnNames = { "Property", "Value" };
		Object[][] rowData = null;
		int fields;
		try {
			fields = Introspector.getBeanInfo(node.getClass()).getPropertyDescriptors().length -1;
			rowData = new Object[fields][2];
		} catch (IntrospectionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		/*
		 * int i = 0; for (Field f : node.getClass().getDeclaredFields()) {
		 * rowData[i][0] = f.getName(); try { rowData[i][1] = f.get(node); }
		 * catch (IllegalArgumentException | IllegalAccessException e1) { //
		 * TODO Auto-generated catch block e1.printStackTrace(); } }
		 */

		try {
			int i = 0;
			for (PropertyDescriptor pd : Introspector.getBeanInfo(node.getClass()).getPropertyDescriptors()) {
				
				if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
					rowData[i][0] = pd.getName();
					System.out.println(pd.getReadMethod().invoke(node));
					rowData[i][1] = pd.getReadMethod().invoke(node);
					i++;
				}
				
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| IntrospectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		JTable table = new JTable(rowData, columnNames);
		table.setEnabled(false);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		mainFrame.setTitle(node.toString());
		mainFrame.getContentPane().removeAll();
		mainFrame.getContentPane().add(scrollPane, "Center");
		mainFrame.setSize(640, 480);
		mainFrame.setVisible(true);

	}

}
