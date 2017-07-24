package org.tes.hkx.tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.tes.hkx.model.HKProject;
import org.tes.hkx.model.files.HkFilesFactory;
import org.tes.hkx.tools.ConfigHolder;

@SuppressWarnings("unused")
public class Main {

	private static boolean debug = false;

	private static ConfigHolder config;
	private static AssetCC2CmdWrapper assetCc2 = new AssetCC2CmdWrapper(
			debug ? "D:\\skywind\\git\\HBT2Skyrim\\out-win32\\AssetCc2.exe" : "AssetCc2.exe");
	private static HkxCmdWrapper hkxcmd = new HkxCmdWrapper(
			debug ? "D:\\skywind\\git\\HBT2Skyrim\\out-win32\\hkxcmd.exe" : "hkxcmd.exe");
	private static HkFilesFactory filesFactory;

	public static Collection<File> getHKXFiles(String Path, boolean recursive) {
		File dir = new File(Path);
		Collection<File> files = FileUtils.listFiles(dir, new IOFileFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".hkx");
			}

			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				return file.getAbsolutePath().toLowerCase().endsWith(".hkx");
			}
		}, recursive ? TrueFileFilter.INSTANCE : null);
		return files;
	}
	
	public static void upgradeBinaryAssets(String InputFileAbsolutePath, String OutputFileAbsolutePath)
			throws Exception {

		// String upgradedProjectAbsolutePath = config.skyrimExportFolderPath +
		// pHKX.getName();
		String[] assetCc2commands = { "-r4101", "\"" + InputFileAbsolutePath + "\"",
				"\"" + OutputFileAbsolutePath + "\"" };
		System.out.println(assetCc2.exec(assetCc2commands));
		// Convert to XML
		File upgradedFile = new File(OutputFileAbsolutePath);
		String xmlUpgradedFilePath = upgradedFile.getParentFile().getAbsolutePath() + File.separator
				+ FilenameUtils.removeExtension(upgradedFile.getName()) + ".xml";
		String[] HkxCmdCommands = { "Convert", "-v:XML", "\"" + OutputFileAbsolutePath + "\"",
				"\"" + xmlUpgradedFilePath + "\"" };
		System.out.println(hkxcmd.exec(HkxCmdCommands));
	}


	public static void upgradeFileWithAssetCC2(String InputFileAbsolutePath, String OutputFileAbsolutePath)
			throws Exception {

		// String upgradedProjectAbsolutePath = config.skyrimExportFolderPath +
		// pHKX.getName();
		String[] assetCc2commands = { "-r4101", "\"" + InputFileAbsolutePath + "\"",
				"\"" + OutputFileAbsolutePath + "\"" };
		System.out.println(assetCc2.exec(assetCc2commands));
		File upgradedFile = new File(OutputFileAbsolutePath);
		String xmlUpgradedFilePath = upgradedFile.getParentFile().getAbsolutePath() + File.separator
				+ FilenameUtils.removeExtension(upgradedFile.getName()) + ".xml";
		System.out.println("XML File:" + xmlUpgradedFilePath);
		// Convert to XML
		String[] HkxCmdCommands = { "Convert", "-v:XML", "\"" + OutputFileAbsolutePath + "\"",
				"\"" + xmlUpgradedFilePath + "\"" };
		System.out.println(hkxcmd.exec(HkxCmdCommands));
		// Adjust Paths
		String content = new String(Files.readAllBytes(Paths.get(xmlUpgradedFilePath)));
		// remove export subpath
		content = content.replaceAll("\\\\Export\\\\", "\\\\");
		// remove relative reference to parent path
		content = content.replaceAll("\\.\\.\\\\", "");
		Files.write(Paths.get(xmlUpgradedFilePath), content.getBytes());
		// Recompile file
		String[] HkxRecompileCommands = { "Convert", "-v:WIN32", "\"" + xmlUpgradedFilePath + "\"",
				"\"" + OutputFileAbsolutePath + "\"" };
		System.out.println(hkxcmd.exec(HkxRecompileCommands));
	}

	public static void upgradeBehaviorFile(String InputFileAbsolutePath, String OutputFileAbsolutePath)
			throws Exception {

		// String upgradedProjectAbsolutePath = config.skyrimExportFolderPath +
		// pHKX.getName();
		String tempBehaviorXMLFilePath = OutputFileAbsolutePath.replace(".hkx", ".xml");
		String[] assetCc2commands = { "-x", "\"" + InputFileAbsolutePath + "\"", "\"" + OutputFileAbsolutePath + "\"" };
		System.out.println(assetCc2.exec(assetCc2commands));
		System.out.println("XML File:" + tempBehaviorXMLFilePath);
		// Convert to WIN32
		String[] HkxCmdCommands = { "Convert", "-v:WIN32", "\"" + tempBehaviorXMLFilePath + "\"",
				"\"" + OutputFileAbsolutePath + "\"" };
		System.out.println(hkxcmd.exec(HkxCmdCommands));
		// Reconvert to XML
		String[] HkxCmdConvertCommands = { "Convert", "-v:XML", "\"" + OutputFileAbsolutePath + "\"",
				"\"" + tempBehaviorXMLFilePath + "\"" };
		System.out.println(hkxcmd.exec(HkxCmdConvertCommands));
		// Adjust Paths
		String content = new String(Files.readAllBytes(Paths.get(tempBehaviorXMLFilePath)));
		// // remove export subpath
		content = content.replaceAll("\\\\Export\\\\", "\\\\");
		// // remove relative reference to parent path
		content = content.replaceAll("\\.\\.\\\\", "");
		Files.write(Paths.get(tempBehaviorXMLFilePath), content.getBytes());
		// // Recompile file
		String[] HkxRecompileCommands = { "Convert", "-v:WIN32", "\"" + tempBehaviorXMLFilePath + "\"",
				"\"" + OutputFileAbsolutePath + "\"" };
		System.out.println(hkxcmd.exec(HkxRecompileCommands));
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Hello from HBT To Skyrim Project Converter!");
		for (String i : args)
			System.out.println("Argument: " + i);
		if (args.length == 8) {
			config = new ConfigHolder(args);
			// Get the project file
			Collection<File> pFile = getHKXFiles(config.HBT_PROJECT_EXPORT_DIR, false);
			// Check project uniqueness
			File updatedProjectFile = null;
			if (pFile.size() == 1) {
				try {
					File pHKX = pFile.iterator().next();
					config.projectFileAbsolutePath = pHKX.getAbsolutePath();
					config.HBT_PROJECT_NAME = FilenameUtils.removeExtension(pHKX.getName());
					System.out.println("Found HKX project: " + config.HBT_PROJECT_NAME);
					config.skyrimExportFolderPath = config.HBT_PROJECT_EXPORT_DIR + "Skyrim" + File.separator
							+ config.HBT_PROJECT_NAME + File.separator;
					System.out.println("Creating Skyrim Export subfolder: " + config.skyrimExportFolderPath);
					FileUtils.deleteDirectory(new File(config.skyrimExportFolderPath));
					FileUtils.forceMkdir(new File(config.skyrimExportFolderPath));
					// Upgrade project files
					Path currentRelativePath = Paths.get("");
					String s = currentRelativePath.toAbsolutePath().toString();
					System.out.println("Current relative path is: " + s);
					updatedProjectFile = new File(config.skyrimExportFolderPath + pHKX.getName());
					upgradeFileWithAssetCC2(pHKX.getAbsolutePath(), config.skyrimExportFolderPath + pHKX.getName());

				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace();
					System.exit(-1);
				}
			} else {
				System.err.println("Unable to find project file, found " + pFile.size() + " files into "
						+ config.HBT_PROJECT_EXPORT_DIR);
				System.err.println("Please check your HBT Project configuration");
				System.err.println(
						"and ensure that the Project Export Dir contains just the project file for this HBT project");

			}
			// Characters
			String charactersExportDir = FilenameUtils.concat(config.HBT_PROJECT_EXPORT_DIR,
					config.HBT_CHARACTERS_EXPORT_DIR);
			Collection<File> pCFiles = getHKXFiles(charactersExportDir, false);
			config.skyrimExportCharactersFolderPath = FilenameUtils.concat(config.skyrimExportFolderPath, "Characters");
			if (!pCFiles.isEmpty())
				FileUtils.forceMkdir(new File(config.skyrimExportCharactersFolderPath));
			for (File pCFile : pCFiles) {
				try {
					String outputFile = FilenameUtils.concat(config.skyrimExportCharactersFolderPath, pCFile.getName());
					upgradeFileWithAssetCC2(pCFile.getAbsolutePath(), outputFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Assets
			String characterAssetsExportDir = FilenameUtils.concat(config.HBT_PROJECT_EXPORT_DIR,
					config.HBT_CHARACTER_ASSETS_EXPORT_DIR);
			Collection<File> pCAFiles = getHKXFiles(characterAssetsExportDir, false);
			config.skyrimExportCharacterAssetsFolderPath = FilenameUtils.concat(config.skyrimExportFolderPath,
					"CharacterAssets");
			if (!pCAFiles.isEmpty())
				FileUtils.forceMkdir(new File(config.skyrimExportCharacterAssetsFolderPath));
			for (File pCAFile : pCAFiles) {
				try {
					String outputFile = FilenameUtils.concat(config.skyrimExportCharacterAssetsFolderPath,
							pCAFile.getName());
					upgradeBinaryAssets(pCAFile.getAbsolutePath(), outputFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Animations
			String AnimationsExportDir = FilenameUtils.concat(config.HBT_PROJECT_EXPORT_DIR,
					config.HBT_ANIMATIONS_EXPORT_DIR);
			Collection<File> pAFiles = getHKXFiles(AnimationsExportDir, false);
			config.skyrimExportAnimationsFolderPath = FilenameUtils.concat(config.skyrimExportFolderPath, "Animations");
			if (!pAFiles.isEmpty())
				FileUtils.forceMkdir(new File(config.skyrimExportAnimationsFolderPath));
			for (File pAFile : pAFiles) {
				try {
					String outputFile = FilenameUtils.concat(config.skyrimExportAnimationsFolderPath, pAFile.getName());
					upgradeFileWithAssetCC2(pAFile.getAbsolutePath(), outputFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Behaviors and Update cache
			String BehaviorsExportDir = FilenameUtils.concat(config.HBT_PROJECT_EXPORT_DIR,
					config.HBT_BEHAVIORS_EXPORT_DIR);
			Collection<File> pBFiles = getHKXFiles(BehaviorsExportDir, false);
			config.skyrimExportBehaviorsFolderPath = FilenameUtils.concat(config.skyrimExportFolderPath, "Behaviors");

			if (!pBFiles.isEmpty())
				FileUtils.forceMkdir(new File(config.skyrimExportBehaviorsFolderPath));
			for (File pBFile : pBFiles) {
				try {
					String outputFile = FilenameUtils.concat(config.skyrimExportBehaviorsFolderPath, pBFile.getName());
					upgradeBehaviorFile(pBFile.getAbsolutePath(), outputFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			String animationCachePath = FilenameUtils.concat(config.HBT_PROJECT_DIR, "animations");
			animationCachePath = FilenameUtils.concat(animationCachePath, "mdata");
			
			System.out.println("Loading Havok Project: "+ updatedProjectFile.getAbsolutePath().replace(".hkx", ".xml"));

			// BETH-O-MATIC 3000
			BehaviorProcessor
					.process(new HKProject(new File(updatedProjectFile.getAbsolutePath().replace(".hkx", ".xml"))));

			System.out.println("Done!");

			for (File pBFile : pBFiles) {
				try {
					String outputFile = FilenameUtils.concat(config.skyrimExportBehaviorsFolderPath, pBFile.getName());
					// recompile behavior
					String[] HkxCmdCommands = { "Convert", "-v:WIN32", "\"" + outputFile.replace(".hkx", ".xml") + "\"",
							"\"" + outputFile + "\"" };
					System.out.println(hkxcmd.exec(HkxCmdCommands));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// Cache
			AnimDataGen.generate(new File(updatedProjectFile.getAbsolutePath().replace(".hkx", ".xml")),
					new File(animationCachePath), new File(config.skyrimExportFolderPath), null, null, config.oblivionMode, config.OutputProjectDataRelative);
			System.out.println("Cache regenerated");
		}
	}

}
