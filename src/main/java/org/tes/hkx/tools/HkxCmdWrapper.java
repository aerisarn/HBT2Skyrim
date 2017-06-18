package org.tes.hkx.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class HkxCmdWrapper extends CMDWrapper {

	public HkxCmdWrapper(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	
	//TODO: proper notifications
	public boolean importProject(File inPath, File outputPath) throws Exception {

		stop = false;
		if (!inPath.isDirectory() || inPath.equals(outputPath))
			return false;
		try {
			FileUtils.forceMkdir(outputPath);
		} catch (Exception e) {
			return false;
		}
		// Search for binary hkx files inside outputPath
		Collection<File> outputfiles = FileUtils.listFiles(inPath, new WildcardFileFilter("*.hkx"),
				TrueFileFilter.TRUE);

		for (File binaryFile : outputfiles) {
			// log("Traslating to XML " + binaryFile.getAbsolutePath());

			if (stop) {
				FileUtils.forceDelete(outputPath);
				return true;
			}
			String cmdparams = " convert -v:XML -o \"" + binaryFile.getParent() + File.separator
					+ FilenameUtils.removeExtension(binaryFile.getName()) + ".xml\"" + " \""
					+ binaryFile.getAbsolutePath() + "\"";

			//TODO
			//execHkxCmd(cmdparams);

			// remove source binary file
			FileUtils.forceDelete(binaryFile);
		}

		return true;
	}

}
