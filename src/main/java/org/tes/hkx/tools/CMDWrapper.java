package org.tes.hkx.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public abstract class CMDWrapper extends Observable implements Observer{
	protected String binaryPath = "";
	protected boolean stop = false;
	
	public String getBinaryPath() {
		return binaryPath;
	}

	public void setBinaryPath(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	@Override
	public void update(Observable o, Object arg) {
		stop = true;
	}
	
	public String exec(String[] parameters) throws Exception {
		String cmd = binaryPath;
		String output = "";

		Runtime rt = Runtime.getRuntime();
		List<String> commands = new ArrayList<>();
		commands.add(cmd);
		for (String s : parameters) {
			commands.add(s);
		}	
		Process proc = rt.exec( commands.toArray(new String[commands.size()]));

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		String s = null;
		while ((s = stdInput.readLine()) != null) {
			output += s + "\n";
		}
		while ((s = stdError.readLine()) != null) {
			output += s + "\n";
		}
		proc.waitFor();
		return output;
	}
	
}
