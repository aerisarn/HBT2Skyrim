package org.tes.hkx.tools;

import java.awt.BorderLayout;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.tes.hkx.model.HKProject;
import org.tes.hkx.model.HkFile;
import org.tes.hkx.model.files.HkAnimationFile;
import org.tes.hkx.model.files.HkBehaviorFile;
import org.tes.hkx.model.files.HkCharacterFile;
import org.tes.hkx.model.files.HkFilesFactory;
import org.tes.hkx.tree.HKXTree;

public class SimpleBehaviorViewer {

	public static void show(HKProject project) throws Exception {

		HkFilesFactory factory = new HkFilesFactory();

		File projectFilePath = null;
		
		Map<String, HkFile> fileMap = new HashMap<>();


		try {
//			HKProject project = new HKProject(projectFilePath);
			projectFilePath = new File(project.getProjectFile().getFileName());
			fileMap.put(projectFilePath.getAbsolutePath(), project.getProjectFile());
			HkBehaviorFile hkbReadOnlyFile = project.getBehaviors(project.getCharacterFiles().get(0)).iterator().next();
			//hkbReadOnlyFile.setFileName(hkbReadOnlyFile.getGraph().getName());

			HKXTree rotree = new HKXTree(hkbReadOnlyFile.getRoot());
			HKXTreeSelectionListener tlistener = new HKXTreeSelectionListener(rotree);
			rotree.addTreeSelectionListener(tlistener);

			// The JTree can get big, so allow it to scroll
			JScrollPane scrollpane = new JScrollPane(rotree);
			
			DefaultMutableTreeNode pRoot = new DefaultMutableTreeNode(projectFilePath);
			DefaultMutableTreeNode pInfo = new DefaultMutableTreeNode("Project");
			pInfo.add(new DefaultMutableTreeNode(projectFilePath.getAbsolutePath()));
			pRoot.add(pInfo);
			DefaultMutableTreeNode cInfo = new DefaultMutableTreeNode("Characters");
			DefaultMutableTreeNode bInfo = new DefaultMutableTreeNode("Behaviors");
			DefaultMutableTreeNode sInfo = new DefaultMutableTreeNode("Skeletons");
			DefaultMutableTreeNode aInfo = new DefaultMutableTreeNode("Animations");
			
			for (HkCharacterFile c : project.getCharacterFiles()) {
				// logger.info("Found Character: " +
				// c.getStringData().getName());
				DefaultMutableTreeNode ccInfo = new DefaultMutableTreeNode(c.getFileName());
				fileMap.put(c.getFileName(), c);
				cInfo.add(ccInfo);

				for (HkBehaviorFile bf : project.getBehaviors(c)) {
					// logger.info("Behavior: " + bf.getGraph().getName());
					DefaultMutableTreeNode bbInfo = new DefaultMutableTreeNode(bf.getFileName());
					bInfo.add(bbInfo);
					fileMap.put(bf.getFileName(), bf);
				}

				// logger.info("Skeleton: " +
				// p.getRigRagdollFile(c).getAnimationContainer().getSkeletonsAt(0).getName());
				DefaultMutableTreeNode ssInfo = new DefaultMutableTreeNode(project.getRigRagdollFile(c).getFileName());
				sInfo.add(ssInfo);
				fileMap.put(project.getRigRagdollFile(c).getFileName(), project.getRigRagdollFile(c));
				// logger.info("Loaded " + p.getAnimationFiles(c).size() + "
				// animations");
				for (HkAnimationFile af : project.getAnimationFiles(c)) {
					DefaultMutableTreeNode aaInfo = new DefaultMutableTreeNode(af.getFileName());
					aInfo.add(aaInfo);
					fileMap.put(af.getFileName(), af);
				}
			}
			
			pRoot.add(cInfo);
			pRoot.add(bInfo);
			pRoot.add(sInfo);
			pRoot.add(aInfo);
			
			JTree ptree = new JTree(pRoot);
			ptree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			ptree.setRootVisible(false);
			
			JFrame frame = new JFrame("ORIGINAL: " + hkbReadOnlyFile.getFileName());
			
			JScrollPane pscrollpane = new JScrollPane(ptree);
			
			HKXProjectSelectionListener plistener = new HKXProjectSelectionListener(scrollpane, rotree, frame,
					fileMap, ptree, tlistener);
			ptree.addTreeSelectionListener(plistener);

			// Display it all in a window and make the window appear
			
			frame.getContentPane().add(scrollpane, BorderLayout.CENTER);
			frame.getContentPane().add(pscrollpane, BorderLayout.PAGE_START);
			frame.setSize(400, 600);
			frame.setVisible(true);
//
//			HKProject project = new HKProject(behaviorFilePath);
			

			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
