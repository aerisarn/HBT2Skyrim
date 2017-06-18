package org.tes.hkx.tools;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.tes.hkx.lib.HkobjectType;
import org.tes.hkx.lib.ext.hkaSplineCompressedAnimation;
import org.tes.hkx.lib.ext.hkbClipGenerator;
import org.tes.hkx.lib.ext.innerAnnotation;
import org.tes.hkx.lib.ext.innerTrackInfo;
import org.tes.hkx.lib.ext.innerTrigger;
import org.tes.hkx.model.HKProject;
import org.tes.hkx.model.files.HkAnimationFile;
import org.tes.hkx.model.files.HkBehaviorFile;
import org.tes.hkx.model.files.HkCharacterFile;
import org.tes.tools.animdataparser.AnimDataFile;
import org.tes.tools.animdataparser.AnimSetDataFile;
import org.tes.tools.animdataparser.ClipGeneratorBlock;
import org.tes.tools.animdataparser.ClipMovementData;
import org.tes.tools.animdataparser.ProjectBlock;
import org.tes.tools.animdataparser.ProjectDataBlock;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class AnimDataGen {

	public static void generate(File projectFile, File animCacheDir, File outputDir, File sourceAnimData,
			File sourceAnimSetData) {
		try {
			System.out.println("Hello World!");
			System.out.println(projectFile.getAbsolutePath());
			System.out.println(animCacheDir.getAbsolutePath());
			System.out.println(outputDir.getAbsolutePath());
			// System.out.println(sourceAnimData);
			// System.out.println(sourceAnimSetData.getName());

			AnimDataFile animData = new AnimDataFile();
			AnimSetDataFile animSetData = new AnimSetDataFile();
			HKProject project = new HKProject(projectFile);
			if (sourceAnimData == null || !sourceAnimData.exists()) {
				InputStream in = AnimDataGen.class.getResourceAsStream("/animationdatasinglefile.txt");
				String theString = IOUtils.toString(in, "windows-1252");
				animData.parse(theString);
			} else {
				String block = new String(Files.readAllBytes(sourceAnimData.toPath()));
				animData.parse(block);
			}
			if (sourceAnimSetData == null || !sourceAnimSetData.exists()) {
				InputStream in = AnimDataGen.class.getResourceAsStream("/animationsetdatasinglefile.txt");
				String theString = IOUtils.toString(in, "windows-1252");
				animSetData.parse(theString);
			} else {
				String block = new String(Files.readAllBytes(sourceAnimSetData.toPath()));
				animSetData.parse(block);
			}

			System.out.println("Searching cache files");

			Collection<File> keyFiles = FileUtils.listFiles(animCacheDir, new IOFileFilter() {
				@Override
				public boolean accept(File file) {
					// TODO Auto-generated method stub
					return file.getName().endsWith(".txt");
				}

				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return name.endsWith(".txt");
				}
			}, TrueFileFilter.INSTANCE);

			System.out.println("Found "+ keyFiles.size() + " cache files");
			
			// Check cache files
			String projectName = FilenameUtils.getBaseName(project.getProjectFile().getFileName()) + ".txt";
			ProjectBlock dataBlock = new ProjectBlock();
			ProjectDataBlock dataMovementBlock = new ProjectDataBlock();
			dataBlock.setHasAnimationCache(!keyFiles.isEmpty());
			dataBlock.setHasProjectFiles(!project.getCharacterFiles().isEmpty());

			System.out.println("Project has cache? "+ dataBlock.isHasProjectFiles());
			
			if (dataBlock.isHasProjectFiles()) {
				for (HkCharacterFile cf : project.getCharacterFiles()) {
					System.out.println("Analyzing "+ cf.getData().getStringData().getName());
					for (HkBehaviorFile bf : project.getBehaviors(cf))
						dataBlock.getProjectFiles().getStrings().add("Behaviors\\"
								+ FilenameUtils.removeExtension(FilenameUtils.getBaseName(bf.getFileName())) + ".hkx");
					dataBlock.getProjectFiles().getStrings().add("Characters\\"
							+ FilenameUtils.removeExtension(FilenameUtils.getBaseName(cf.getFileName())) + ".hkx");
					dataBlock.getProjectFiles().getStrings()
							.add("CharacterAssets\\"
									+ FilenameUtils.removeExtension(
											FilenameUtils.getBaseName(project.getRigRagdollFile(cf).getFileName()))
									+ ".hkx");
				}
			}
			HkCharacterFile character = project.getCharacterFiles().get(0);
			// map all clips to their clip file name
			Map<String, hkaSplineCompressedAnimation> hkAnimFileMap = new HashMap<>();
			for (HkAnimationFile af : project.getAnimationFiles(character)) {
				for (HkobjectType aobj : af.getObjects()) {
					if (aobj instanceof hkaSplineCompressedAnimation) {
						System.out.println(FilenameUtils.removeExtension(FilenameUtils.getBaseName(af.getFileName()))
								.toLowerCase());
						hkAnimFileMap.put(
								FilenameUtils
										.removeExtension(FilenameUtils.getBaseName(af.getFileName()).toLowerCase()),
								(hkaSplineCompressedAnimation) aobj);
					}
				}
			}
			int index = 0;
			Map<String, ClipMovementData> clipMovementData = new HashMap<>();
			for (File cacheFile : keyFiles) {
				String key = FilenameUtils
						.removeExtension(FilenameUtils.getBaseName(cacheFile.getName()).toLowerCase());
				String block = new String(Files.readAllBytes(cacheFile.toPath()));
				Scanner keyFileScanner = new Scanner(block);
				hkaSplineCompressedAnimation thisAnim = hkAnimFileMap.get(key);
				if (thisAnim == null) {
					keyFileScanner.close();
					throw new Exception("Unmapped Cache File!" + key);
				}
				// TODO: handle empty file
				boolean rot = false;
				ClipMovementData thisMovementData = new ClipMovementData();
				thisMovementData.setCacheIndex(index);
				thisMovementData.setDuration(thisAnim.getDuration());
				while (keyFileScanner.hasNextLine()) {
					String line = keyFileScanner.nextLine();
					if (line.isEmpty()) {
						rot = true;
						continue;
					}
					if (rot)
						thisMovementData.getRotations().getStrings()
								.add(line.replaceAll(" \\(quat", "").replaceAll("\\)", ""));
					else
						thisMovementData.getTraslations().getStrings().add(line);
				}
				clipMovementData.put(key, thisMovementData);
				keyFileScanner.close();
				dataMovementBlock.getMovementData().add(thisMovementData);
				index++;
			}
			// behaviors has multiple nodes for the same clip
			for (HkBehaviorFile bf : project.getBehaviors(character)) {
				for (HkobjectType obj : bf.getObjects()) {
					if (obj instanceof hkbClipGenerator) {
						hkbClipGenerator clip = (hkbClipGenerator) obj;
						String key = FilenameUtils
								.removeExtension(FilenameUtils.getBaseName(clip.getAnimationName()).toLowerCase());
						ClipGeneratorBlock b = new ClipGeneratorBlock();
						ClipMovementData thisMovementData = clipMovementData.get(key);
						if (thisMovementData == null)
							throw new Exception("Cannot find movement data for key: " + key);
						hkaSplineCompressedAnimation thisAnim = hkAnimFileMap.get(key);
						if (thisAnim == null)
							throw new Exception("Cannot find animation data for key: " + key);
						b.setCacheIndex(thisMovementData.getCacheIndex());
						b.setName(clip.getName());
						b.setPlaybackSpeed(clip.getPlaybackSpeed());
						b.setUnknown("0");
						b.setUnknown2("0");
						// events ordering
						// Anim annotations
						float duration = Float.parseFloat(thisAnim.getDuration());
						Multimap<Float, String> items = TreeMultimap.create();
						if (thisAnim.getAnnotationTracks() != null) {
							for (innerTrackInfo at : thisAnim.getAnnotationTracks()) {
								for (innerAnnotation an : at.getAnnotations()) {
									items.put(Float.parseFloat(an.getTime()), an.getText());
								}
							}
						}
						// Clip annotation
						if (clip.getTriggers() != null) {
							for (innerTrigger trigger : clip.getTriggers().getTriggers()) {
								Float triggerTime = Float.parseFloat(trigger.getLocalTime());
								if (Boolean.parseBoolean(trigger.getRelativeToEndOfClip())) {
									triggerTime = duration + triggerTime;
								}
								items.put(triggerTime, bf.getGraphData().getStringData()
										.getEventNamesAt(Integer.valueOf(trigger.getEvent().getId())));
							}
						}
						// Fill the data
						for (Entry<Float, String> e : items.entries()) {
							b.getEvents().getStrings().add(e.getValue() + ":" + e.getKey());
						}
						dataBlock.getClips().add(b);
					}
				}
			}
			// create subdirs
			String animDataPath = FilenameUtils.concat(outputDir.getAbsolutePath(), "animationdata");
			String animDataFilePath = FilenameUtils.concat(animDataPath, projectName);
			FileUtils.forceMkdir(new File(animDataPath));
			PrintWriter out = new PrintWriter(animDataFilePath);
			out.write(dataBlock.getBlock());
			out.flush();
			out.close();

			String boundAnimsDataPath = FilenameUtils.concat(animDataPath, "boundanims");
			FileUtils.forceMkdir(new File(boundAnimsDataPath));
			String boundAnimsDataFilePath = FilenameUtils.concat(boundAnimsDataPath, "anims_" + projectName);
			out = new PrintWriter(boundAnimsDataFilePath);
			out.write(dataMovementBlock.getBlock());
			out.flush();
			out.close();

			// Add to animData
			Integer dataIndex = 0;
			boolean found = false;
			for (String projectIterator : animData.getProjectList().getStrings()) {
				if (projectIterator.equals(projectName)) {
					found = true;
					animData.getProjectBlockList().set(dataIndex, dataBlock);
					animData.getProjectMovementBlockList().put(dataIndex, dataMovementBlock);
				}
				dataIndex++;
			}
			if (!found) {
				dataIndex = animData.getProjectList().getStrings().size();
				animData.getProjectList().getStrings().add(projectName);
				animData.getProjectBlockList().add(dataBlock);
				animData.getProjectMovementBlockList().put(dataIndex, dataMovementBlock);
			}
			String newAnimDataPath = FilenameUtils.concat(outputDir.getAbsolutePath(), "animationdatasinglefile.txt");
			out = new PrintWriter(newAnimDataPath);
			out.write(animData.toString());
			out.flush();
			out.close();

			// System.out.println("############################ANIMSETDATA#########################################");
			// // // int numLines = 0;
			// // // System.out.println("1");
			// // // System.out.println("FullCharacter.txt");
			// // // System.out.println("V3");
			// // // System.out.println("0");
			// // // System.out.println("0");
			// // // System.out.println("0");
			// // // System.out.println(
			// // //
			// //
			// minotaurProject.getCharacterFiles().iterator().next().getStringData().getNumAnimationNames());
			// // // for (String animationFile :
			// // //
			// //
			// minotaurProject.getCharacterFiles().iterator().next().getStringData()
			// // // .getAnimationNames()) {
			// // // File anim = new File(minotaurFile.getParent(),
			// // // animationFile.toLowerCase().replace(".hkx", ".xml"));
			// // // System.out.println(Long.decode("0x" +
			// // // crc.compute("meshes\\actors\\minotaur\\animations")));
			// // // System.out.println(Long.decode("0x" +
			// // //
			// crc.compute(FilenameUtils.removeExtension(anim.getName()))));
			// // // System.out.println("7891816");
			// // // }
			//
			// factory.save(hkbNew, new File(
			// "/home/edocan/PlayOnLinux's virtual drives/Steam/drive_c/Program
			// Files/Steam/steamapps/common/Skyrim/Data/meshes/actors/Minotaur/behaviors/Minotaur_behavior.xml"));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
