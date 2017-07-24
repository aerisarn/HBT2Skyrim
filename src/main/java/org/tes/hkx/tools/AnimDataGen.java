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
import org.tes.hkx.lib.ext.hkbStateMachine;
import org.tes.hkx.lib.ext.hkbStateMachineStateInfo;
import org.tes.hkx.lib.ext.hkbStateMachineTransitionInfoArray;
import org.tes.hkx.lib.ext.innerAnnotation;
import org.tes.hkx.lib.ext.innerStateTransitionInfo;
import org.tes.hkx.lib.ext.innerTrackInfo;
import org.tes.hkx.lib.ext.innerTrigger;
import org.tes.hkx.model.HKProject;
import org.tes.hkx.model.files.HkAnimationFile;
import org.tes.hkx.model.files.HkBehaviorFile;
import org.tes.hkx.model.files.HkCharacterFile;
import org.tes.hkx.model.visitors.GraphFSMVisitor;
import org.tes.tools.animdataparser.AnimDataFile;
import org.tes.tools.animdataparser.AnimSetDataFile;
import org.tes.tools.animdataparser.AttackDataBlock;
import org.tes.tools.animdataparser.ClipGeneratorBlock;
import org.tes.tools.animdataparser.ClipMovementData;
import org.tes.tools.animdataparser.ProjectAttackBlock;
import org.tes.tools.animdataparser.ProjectAttackListBlock;
import org.tes.tools.animdataparser.ProjectBlock;
import org.tes.tools.animdataparser.ProjectDataBlock;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class AnimDataGen {

	public static String getEventName(String eventName, boolean OblivionMode) {
		String sanitized = eventName.replaceAll("\\p{C}", "")
		.replaceAll(": ", "_").replaceAll(":", "_");
		if (OblivionMode) {
			if (sanitized.toLowerCase().contains("enum")) {
				if (sanitized.toLowerCase().contains("left"))
					return "syncLeft";
				if (sanitized.toLowerCase().contains("right"))
					return "syncRight";
			}
		}
		return sanitized;
	}
	
	public static void generate(File projectFile, File animCacheDir, File outputDir, File sourceAnimData,
			File sourceAnimSetData, boolean oblivionMode, String AnimRelPathOutput) {
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
			
			int c =0;
			int hagind = -1;
			for (String s: animSetData.getProjectsList().getStrings()) {
				if (s.toLowerCase().contains("hag")) {
					hagind = c;
				}
				c++;
			}
			if (hagind>0) {
				ProjectAttackListBlock l = animSetData.getProjectAttackList().get(hagind);
				l.toString();
			}

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
			ProjectAttackBlock attackDataBlock = new ProjectAttackBlock();
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
			
			Map<Integer, String> attackEvents = new HashMap<>();
			// behavior has multiple nodes for the same clip
			for (HkBehaviorFile bf : project.getBehaviors(character)) {
				//Attacks Cache
				int eventId = 0;
				for (String event : bf.getGraphData().getStringData().getEventNames()) {
					if (event.startsWith("attackStart")) {
						attackEvents.put(eventId, event);
					}
					eventId++;
				}
				ArrayListMultimap<hkbStateMachineStateInfo, hkbClipGenerator> statesClipMap = ArrayListMultimap.create();
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
									String eventName = getEventName(an.getText(), oblivionMode);
									items.put(Float.parseFloat(an.getTime()), eventName);
									if (oblivionMode && eventName.contains("sync")) {
										if (eventName.contains("Left"))
											items.put(Float.parseFloat(an.getTime()), "FootLeft");
										if (eventName.contains("Right"))
											items.put(Float.parseFloat(an.getTime()), "FootRight");
									}
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
						
						//State-Clip Map
						statesClipMap.put(clip.findParentWithClass(hkbStateMachineStateInfo.class), clip);
					}
					//Find all clips who are involved into TO/Nested TO attack state clip playing. Tricky
					else if (obj instanceof hkbStateMachineTransitionInfoArray) {
						hkbStateMachineTransitionInfoArray transition = (hkbStateMachineTransitionInfoArray)obj;
						for (innerStateTransitionInfo inner : transition.getTransitions()) {
							if (attackEvents.get(Integer.valueOf(inner.getEventId()))!=null) {
								AttackDataBlock attackClipData = new AttackDataBlock();
								System.out.println("Found Attack Transition: "+inner);
								attackClipData.setEventName(attackEvents.get(Integer.valueOf(inner.getEventId())));
								attackClipData.setUnk1(0);
								hkbStateMachine fsm = inner.findParentWithClass(hkbStateMachine.class);
								hkbStateMachineStateInfo state = fsm.findState(Integer.valueOf(inner.getToStateId()));
								hkbStateMachine innerfsm = state.accept(new GraphFSMVisitor());
								if (innerfsm!= null) {
									hkbStateMachineStateInfo innerState = innerfsm.findState(Integer.valueOf(inner.getToNestedStateId()));
									if (innerState!=null)
										state = innerState;
								}
								for (hkbClipGenerator clip : statesClipMap.get(state)) {
									System.out.println("Related clip: "+clip);
									attackClipData.clips.getStrings().add(clip.getName());
								}
								attackDataBlock.getAttackData().getAttackData().add(attackClipData);
								attackDataBlock.getAttackData().blocks=attackDataBlock.getAttackData().blocks+1;
							}
 						}
					}
				}
	
			}
			
			HkCRC crc = new HkCRC();
			
			for (HkAnimationFile f : project.getAnimationFiles(character)) {		
				String relative = outputDir.toURI().relativize(new File(f.getFileName()).toURI()).getPath();
				relative = FilenameUtils.normalizeNoEndSeparator(relative);
				
				String outputPath = FilenameUtils.concat(FilenameUtils.normalizeNoEndSeparator(AnimRelPathOutput),FilenameUtils.getPathNoEndSeparator(relative));
				String outputName = FilenameUtils.getBaseName(relative);
				
				System.out.println(outputPath.toLowerCase());
				System.out.println(outputName.toLowerCase());
				
				attackDataBlock.getCrc32Data().getStrings().add(String.valueOf(
						Long.decode("0x" + crc.compute(outputPath.toLowerCase()))));
				attackDataBlock.getCrc32Data().getStrings().add(String.valueOf(
						Long.decode("0x" + crc.compute(outputName))));
				attackDataBlock.getCrc32Data().getStrings().add("7891816");
				
			}
			
			//ANIMSETDATA
			// create subdirs
			String animSetDataPath = FilenameUtils.concat(outputDir.getAbsolutePath(), "animationsetdata");
			String animSetDataProjectPath = FilenameUtils.concat(animSetDataPath, FilenameUtils.getBaseName(project.getProjectFile().getFileName())+"Data");
			FileUtils.forceMkdir(new File(animSetDataProjectPath));
			String animSetDataFileName = FilenameUtils.concat(animSetDataProjectPath, projectName);
			PrintWriter out = new PrintWriter(animSetDataFileName);
			out.write("FullCharacter.txt");
			out.flush();
			out.close();
			
			out = new PrintWriter(FilenameUtils.concat(animSetDataProjectPath,"FullCharacter.txt"));
			out.write(attackDataBlock.getBlock());
			out.flush();
			out.close();
			
			
			
			ProjectAttackListBlock p = new ProjectAttackListBlock();
			p.getProjectFiles().getStrings().add("FullCharacter.txt");
			p.getProjectAttackBlocks().add(attackDataBlock);
			
			// Add to animData
			Integer adataIndex = 0;
			boolean afound = false;
			String innerProjectName = FilenameUtils.concat(FilenameUtils.getBaseName(project.getProjectFile().getFileName())+"Data",projectName);
			for (String projectIterator : animSetData.getProjectsList().getStrings()) {
				if (projectIterator.equals(innerProjectName)) {
					afound = true;
					animSetData.getProjectAttackList().set(adataIndex, p);
				}
				adataIndex++;
			}
			if (!afound) {
				animSetData.getProjectsList().getStrings().add(innerProjectName);
				animSetData.getProjectAttackList().add(p);
			}
			String newAnimSetDataPath = FilenameUtils.concat(outputDir.getAbsolutePath(), "animationsetdatasinglefile.txt");
			out = new PrintWriter(newAnimSetDataPath);
			out.write(animSetData.toString());
			out.flush();
			out.close();
			
			//ANIMDATA
			String[] lastLine;
			// fix
			for (ClipMovementData thisMovementData : dataMovementBlock.getMovementData()) {
				if (thisMovementData.getTraslations().getStrings().isEmpty()) {
					thisMovementData.getTraslations().getStrings().add(thisMovementData.getDuration() + " 0 0 0");
				} else {
					lastLine = thisMovementData.getTraslations().getStrings().get(thisMovementData.getTraslations().getStrings().size()-1).split(" ");
					if (lastLine[0] != thisMovementData.getDuration()) {
						thisMovementData.getTraslations().getStrings().add(thisMovementData.getDuration() + 
								" " +lastLine[1]+" "+ lastLine[2]+" " +lastLine[3]);
					}
				}
				if (thisMovementData.getRotations().getStrings().isEmpty()) {
					thisMovementData.getRotations().getStrings().add(thisMovementData.getDuration() + " 0 0 0 1");
				} else {
					lastLine = thisMovementData.getRotations().getStrings().get(thisMovementData.getRotations().getStrings().size()-1).split(" ");
					if (lastLine[0] != thisMovementData.getDuration()) {
						thisMovementData.getRotations().getStrings().add(thisMovementData.getDuration() + 
								" " +lastLine[1]+" "+ lastLine[2]+" " +lastLine[3]+" " +lastLine[4]);
					}
				}
			}

			// create subdirs
			String animDataPath = FilenameUtils.concat(outputDir.getAbsolutePath(), "animationdata");
			String animDataFilePath = FilenameUtils.concat(animDataPath, projectName);
			FileUtils.forceMkdir(new File(animDataPath));
			out = new PrintWriter(animDataFilePath);
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
			
			//ANIMSETDATA
			
			
			

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
