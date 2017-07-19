package org.tes.hkx.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tes.hkx.lib.HkobjectType;
import org.tes.hkx.lib.ext.BSRagdollContactListenerModifier;
import org.tes.hkx.lib.ext.BSSpeedSamplerModifier;
import org.tes.hkx.lib.ext.hkMemoryResourceContainer;
import org.tes.hkx.lib.ext.hkMemoryResourceHandle;
import org.tes.hkx.lib.ext.hkRootLevelContainer;
import org.tes.hkx.lib.ext.hkbBoneWeightArray;
import org.tes.hkx.lib.ext.hkbEventDrivenModifier;
import org.tes.hkx.lib.ext.hkbKeyframeBonesModifier;
import org.tes.hkx.lib.ext.hkbModifierList;
import org.tes.hkx.lib.ext.hkbPoseMatchingGenerator;
import org.tes.hkx.lib.ext.hkbPoweredRagdollControlsModifier;
import org.tes.hkx.lib.ext.hkbRigidBodyRagdollControlsModifier;
import org.tes.hkx.lib.ext.hkbVariableBindingSet;
import org.tes.hkx.lib.ext.hkxCamera;
import org.tes.hkx.lib.ext.hkxIndexBuffer;
import org.tes.hkx.lib.ext.hkxMaterial;
import org.tes.hkx.lib.ext.hkxMesh;
import org.tes.hkx.lib.ext.hkxMeshSection;
import org.tes.hkx.lib.ext.hkxNode;
import org.tes.hkx.lib.ext.hkxScene;
import org.tes.hkx.lib.ext.hkxSkinBinding;
import org.tes.hkx.lib.ext.hkxTextureFile;
import org.tes.hkx.lib.ext.hkxVertexBuffer;
import org.tes.hkx.lib.ext.innerEvent;
import org.tes.hkx.lib.ext.innerFieldVariants;
import org.tes.hkx.lib.ext.innerVariableBinding;
import org.tes.hkx.lib.ext.innerWorldFromModelModeData;
import org.tes.hkx.model.HKProject;
import org.tes.hkx.model.IHkContainer;
import org.tes.hkx.model.IHkVisitable;
import org.tes.hkx.model.files.HkBehaviorFile;
import org.tes.hkx.model.files.HkCharacterFile;
import org.tes.hkx.model.files.HkSkeletonFile;

public class BehaviorProcessor {

	static String STATE_VARIABLE_NAME = "iState";
	static String DIRECTION_VARIABLE_NAME = "Direction";
	static String SPEED_VARIABLE_NAME = "Speed";
	static String SPEED_SAMPLED_VARIABLE_NAME = "SpeedSampled";
	
	static String RAGDOLL_EVENT_NAME = "Ragdoll";

	static String STATE_MOD_PATH = "state";
	static String DIRECTION_MOD_PATH = "direction";
	static String SPEED_MOD_PATH = "goalSpeed";
	static String SPEEDSAMPLED_MOD_PATH = "speedOut";
	
	static String FULL_RAGDOLL_EDM_NAME = "FullRagdollEDM";
	static String POWERED_RAGDOLL_NO_MATCHING = "PoweredRagdollNoMatching";
	static String POWERED_RAGDOLL_MATCHING = "PoweredRagdollMatching";
	

	static void process(HKProject project) {
		System.out.println("B-O-M 3000 Activated!");
		try {
			for (HkCharacterFile character : project.getCharacterFiles()) {
				System.out.println("Processing character: " + character.getStringData().getName());
				boolean foundBSSpeedSamplerModifier = false;
				HkBehaviorFile rootBehavior = null;
				for (HkBehaviorFile behavior : project.getBehaviors(character)) {

//					if (FilenameUtils.getBaseName(FilenameUtils.getName(behavior.getFileName()))
//							.equals("RootBehavior")) {
//						System.out.println("Loading Sample");
//						HkBehaviorFile sample = project.getFilesFactory().loadTypedFile(
//								BehaviorProcessor.class.getResourceAsStream("/RootBehavior.xml"), HkBehaviorFile.class);
//						hkbBehaviorGraphData sampledata = sample.getGraphData();
//						hkbVariableValueSet initValues = sample.getGraphData().getVariableInitialValues();
//						hkbBehaviorGraphStringData stringdata = sample.getGraphData().getStringData();
//						System.out.println(stringdata.getNumVariableNames());
//						int size = stringdata.getNumVariableNames();
//						int lol = size-32;
//						for (int i = lol; i < size; i++) {
//							if (lol < stringdata.getNumVariableNames() && stringdata.getVariableNamesAt(lol)!=null) {
//								System.out.println(i + ": " + stringdata.removeFromVariableNamesAt(lol));
//								sampledata.removeFromVariableInfos(sampledata.getVariableInfosAt(lol));
//								initValues.removeFromWordVariableValues(initValues.getWordVariableValuesAt(lol));
//							}
//						}
//						stringdata.removeFromVariableNamesAt(stringdata.getNumVariableNames()-1);
//						stringdata.addToVariableNames("iState_SiltStriderDefault");
//						
//						System.out.println(stringdata.getNumVariableNames());
//						System.out.println(sampledata.getNumVariableInfos());
//						behavior.getObjects().add(sampledata);
//						behavior.getObjects().add(stringdata);
//						behavior.getObjects().add(initValues);
//						behavior.getGraph().setData(sampledata);
//						rootBehavior = behavior;
//					}

					// Find binding variables
					int stateId = -1;
					int directionId = -1;
					int speedId = -1;
					int speedSampledId = -1;
					
					int ragdollEventId = -1;
					
					int index = 0;
					for (String variable : behavior.getGraphData().getStringData().getVariableNames()) {
						System.out.println(variable);
						if (variable.equals(STATE_VARIABLE_NAME)) {
							stateId = index;
						}
						if (variable.equals(DIRECTION_VARIABLE_NAME)) {
							directionId = index;
						}
						if (variable.equals(SPEED_VARIABLE_NAME)) {
							speedId = index;
						}
						if (variable.equals(SPEED_SAMPLED_VARIABLE_NAME)) {
							speedSampledId = index;
						}
						index++;
					}
					
					index = 0;
					for (String event : behavior.getGraphData().getStringData().getEventNames()) {
						if (event.equals(RAGDOLL_EVENT_NAME))
							ragdollEventId = index;
						index++;
					}
					
					if (stateId < 0 || directionId < 0 || speedId < 0 || speedSampledId < 0) {
						continue;
					}

					hkbModifierList toReplace = null;
					hkbKeyframeBonesModifier toReplaceBSRagdollContactListener = null;
					hkbEventDrivenModifier fullyRagdollEDMSetUserData = null;
					hkbPoseMatchingGenerator poseMatcher = null;
					Set<hkbPoweredRagdollControlsModifier> PRCMs = new HashSet<>();
					Set<hkbRigidBodyRagdollControlsModifier> RBRCMs = new HashSet<>();

					for (HkobjectType o : behavior.getObjects()) {
						if (o instanceof hkbPoseMatchingGenerator) {
							poseMatcher = (hkbPoseMatchingGenerator)o;
						}
						if (o instanceof hkbPoweredRagdollControlsModifier) {
							PRCMs.add((hkbPoweredRagdollControlsModifier)o);
						}
						if (o instanceof hkbRigidBodyRagdollControlsModifier) {
							RBRCMs.add((hkbRigidBodyRagdollControlsModifier)o);
						}
						//Sampler
						if (o instanceof hkbModifierList) {
							hkbModifierList m = (hkbModifierList)o;
							if (m.getName().equals("BSSpeedSamplerModifier")) {
								toReplace = m;
								foundBSSpeedSamplerModifier = true;
							}
						}
						if (o instanceof hkbKeyframeBonesModifier) {
							hkbKeyframeBonesModifier m = (hkbKeyframeBonesModifier)o;
							if (m.getName().equals("BSRagdollContactListenerModifier")) {
								toReplaceBSRagdollContactListener = m;
							}
						}
						if (o instanceof hkbEventDrivenModifier) {
							hkbEventDrivenModifier m = (hkbEventDrivenModifier)o;
							if (m.getName().equals(FULL_RAGDOLL_EDM_NAME)) {
								fullyRagdollEDMSetUserData = m;
							}
						}
					}
					if (poseMatcher != null) {
						poseMatcher.setStartMatchingEventId(String.valueOf(ragdollEventId));
					}
					
					for (hkbPoweredRagdollControlsModifier m : PRCMs) {
						m.setUserData("1");
						if (m.getName().equals(POWERED_RAGDOLL_NO_MATCHING)
								||m.getName().equals(POWERED_RAGDOLL_MATCHING)) {
							hkbBoneWeightArray ba = m.getBoneWeights();
							List<Integer> ids = new ArrayList<>();
							int bindex = 0;
							for (String v : ba.getBoneWeights()) {
								if (Float.parseFloat(v) != 0.0) {
									ids.add(bindex);
								}
								bindex++;
							}
							innerWorldFromModelModeData md = m.getWorldFromModelModeData();
							if (ids.size()==3) {
							md.setPoseMatchingBone0(String.valueOf(ids.get(0)));
							md.setPoseMatchingBone1(String.valueOf(ids.get(1)));
							md.setPoseMatchingBone2(String.valueOf(ids.get(2)));
							}
							if (m.getName().equals(POWERED_RAGDOLL_NO_MATCHING))
								md.setMode("WORLD_FROM_MODEL_MODE_RAGDOLL");
							else if (m.getName().equals(POWERED_RAGDOLL_MATCHING))
								md.setMode("WORLD_FROM_MODEL_MODE_COMPUTE");
							m.setBoneWeights(null);							
						}
						
					}
					for (hkbRigidBodyRagdollControlsModifier m : RBRCMs) {
						m.setUserData("1");
					}
					
					if (fullyRagdollEDMSetUserData != null) {
						fullyRagdollEDMSetUserData.setUserData("1"); //hardcoded
					}
					
					if (toReplaceBSRagdollContactListener != null) {
						BSRagdollContactListenerModifier newM = behavior.createObject(BSRagdollContactListenerModifier.class);
						newM.setUserData("2"); //hardcoded
						innerEvent ev = new innerEvent();
						ev.setId(String.valueOf(ragdollEventId));
						ev.setPayload("null");
						newM.setContactEvent(ev);
						newM.setBones(toReplaceBSRagdollContactListener.getKeyframedBonesList());
						
						for (IHkVisitable po : toReplaceBSRagdollContactListener.getParents()) {
							System.out.println(po.getClass().getName());
							if (po instanceof IHkContainer) {
								((IHkContainer) po).remove(toReplaceBSRagdollContactListener);
							}
							if (po instanceof hkbModifierList) {
								((hkbModifierList) po).addToModifiers(newM);
							}
						}
					}
					
					if (toReplace != null) {

						// Bind
						innerVariableBinding stateBind = new innerVariableBinding();
						stateBind.setMemberPath(STATE_MOD_PATH);
						stateBind.setVariableIndex(String.valueOf(stateId));
						System.out.println(stateBind);

						innerVariableBinding directionBind = new innerVariableBinding();
						directionBind.setMemberPath(DIRECTION_MOD_PATH);
						directionBind.setVariableIndex(String.valueOf(directionId));
						System.out.println(directionBind);

						innerVariableBinding speedBind = new innerVariableBinding();
						speedBind.setMemberPath(SPEED_MOD_PATH);
						speedBind.setVariableIndex(String.valueOf(speedId));
						System.out.println(speedBind);

						innerVariableBinding speedSampledBind = new innerVariableBinding();
						speedSampledBind.setMemberPath(SPEEDSAMPLED_MOD_PATH);
						speedSampledBind.setVariableIndex(String.valueOf(speedSampledId));
						System.out.println(speedSampledBind);

						hkbVariableBindingSet bindArray = behavior.createObject(hkbVariableBindingSet.class);
						bindArray.addToBindings(stateBind);
						bindArray.addToBindings(directionBind);
						bindArray.addToBindings(speedBind);
						bindArray.addToBindings(speedSampledBind);

						BSSpeedSamplerModifier movementController = behavior.createObject(BSSpeedSamplerModifier.class);
						movementController.setVariableBindingSet(bindArray);

						for (IHkVisitable po : toReplace.getParents()) {
							System.out.println(po.getClass().getName());
							if (po instanceof IHkContainer) {
								((IHkContainer) po).remove(toReplace);
							}
							if (po instanceof hkbModifierList) {
								((hkbModifierList) po).addToModifiers(movementController);
							}
						}

						project.getFilesFactory().save(behavior, new File(behavior.getFileName()));
					}
				}

				HkSkeletonFile skeleton = project.getRigRagdollFile(character);

				// clean skeleton
				List<HkobjectType> toRemove = new ArrayList<>();
				innerFieldVariants toRemoveVar = null;
				innerFieldVariants toRemoveMK = null;
				for (HkobjectType so : skeleton.getObjects()) {
					if (so instanceof hkxCamera || so instanceof hkxIndexBuffer || so instanceof hkxMaterial
							|| so instanceof hkxMesh || so instanceof hkxMeshSection || so instanceof hkxScene
							|| so instanceof hkxNode || so instanceof hkxSkinBinding || so instanceof hkxVertexBuffer
							|| so instanceof hkxTextureFile || so instanceof hkMemoryResourceContainer
							|| so instanceof hkMemoryResourceHandle)
						toRemove.add(so);
					if (so instanceof hkRootLevelContainer) {
						hkRootLevelContainer root = (hkRootLevelContainer) so;
						for (innerFieldVariants iv : root.getNamedVariants()) {
							if (iv.getClassName().equals("hkxScene")) {
								toRemoveVar = iv;
							}
							if (iv.getClassName().equals("hkMemoryResourceContainer")) {
								toRemoveMK = iv;
							}
						}
					}

				}
				// HBT6.6 seems to be unable to export skeleton without Scene
				// Data, but skyrim won't need it
				if (toRemoveVar != null)
					skeleton.getRoot().removeFromNamedVariants(toRemoveVar);
				if (toRemoveMK != null)
					skeleton.getRoot().removeFromNamedVariants(toRemoveMK);
				for (HkobjectType so : toRemove)
					skeleton.deleteObject(so);

				if (rootBehavior != null) {
					project.getFilesFactory().save(rootBehavior, new File(rootBehavior.getFileName()));
				}

				project.getFilesFactory().save(skeleton, new File(skeleton.getFileName()));
				//SimpleBehaviorViewer.show(project);
				if (!foundBSSpeedSamplerModifier) {
					System.out.println("Speed Sampler not found");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
