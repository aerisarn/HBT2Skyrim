package org.tes.hkx.tools;

public class ConfigHolder {
	
	//Program Arguments
	public String HBT_PROJECT_EXPORT_DIR;
	public String HBT_CHARACTERS_EXPORT_DIR;
	public String HBT_CHARACTER_ASSETS_EXPORT_DIR;
	public String HBT_BEHAVIORS_EXPORT_DIR;
	public String HBT_ANIMATIONS_EXPORT_DIR;
	public String HBT_PROJECT_DIR;
	public boolean oblivionMode;
	public String OutputProjectDataRelative;
	
	//Project Name
	public String HBT_PROJECT_NAME;
	
	//Files
	public String projectFileAbsolutePath;
	
	public String skyrimExportFolderPath;
	public String skyrimExportCharactersFolderPath;
	public String skyrimExportCharacterAssetsFolderPath;
	public String skyrimExportBehaviorsFolderPath;
	public String skyrimExportAnimationsFolderPath;

	
	
	public ConfigHolder(String[] array) {
		HBT_PROJECT_EXPORT_DIR = array[0];
		HBT_CHARACTERS_EXPORT_DIR = array[1];
		HBT_CHARACTER_ASSETS_EXPORT_DIR = array[2];
		HBT_BEHAVIORS_EXPORT_DIR = array[3];
		HBT_ANIMATIONS_EXPORT_DIR = array[4];
		HBT_PROJECT_DIR = array[5];
		oblivionMode = Boolean.parseBoolean(array[6]);
		OutputProjectDataRelative = array[7];
	}
}
