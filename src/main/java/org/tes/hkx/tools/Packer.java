package org.tes.hkx.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogicgames.packr.Packr;
import com.badlogicgames.packr.PackrConfig;

public class Packer {

	public static void main(String[] args) {
		PackrConfig config = new PackrConfig();
		config.platform = PackrConfig.Platform.Windows32;
		config.jdk = "C:\\Program Files (x86)\\Java\\jdk1.8.0_111.zip";
		config.executable = "HBT2Skyrim";
		config.classpath = Arrays.asList("target\\HBT2Skyrim-0.0.1-SNAPSHOT-jar-with-dependencies.jar");
		config.mainClass = "org.tes.hkx.tools.Main";
		config.vmArgs = Arrays.asList("Xmx1G");
		config.minimizeJre = null; //"soft";
		List<File> binUtils = new ArrayList<>();
		binUtils.add(new File("binutils\\AssetCc2.exe"));
		binUtils.add(new File("binutils\\hkxcmd.exe"));
		config.resources = binUtils;
		config.outDir = new java.io.File("out-win32");

		try {
			System.out.println("Assembling the exe...");
			new Packr().pack(config);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
