package main.java.molab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main {
	
	/**
	 * @param first_address
	 * @param file_path */
	public static void main(String[] args) {
		// read record from file line by line
		File file = new File(args[1]);
		if(file.isFile() && file.exists()) {
			try {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    System.out.print(line);
                    String[] data = line.split(":");
                    String command = "multichain-cli huihong sendassetfrom " + data[0] + " " + args[0] + " huihong " + data[1];
//                    String command = "multichain-cli hmchain sendassetfrom " + data[0] + " " + args[0] + " hmc " + data[1]; // for local test
                    Process proc = Runtime.getRuntime().exec(command); 
        			ArrayList<String> errorOutput = new ArrayList<String>();
                    ArrayList<String> stdOutput = new ArrayList<String>();
                    int status = grabProcessOutput(proc, errorOutput, stdOutput, true);
        			if (status == 0) {
        				// success
        				System.out.println(" success.");
        			} else {
        				System.out.println(" failure.");
        			}
                }
                read.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static int grabProcessOutput(final Process process, final ArrayList<String> errorOutput,
			final ArrayList<String> stdOutput, boolean waitForReaders) throws InterruptedException {
		// read the lines as they come. if null is returned, it's
		// because the process finished
		Thread t1 = new Thread("") { //$NON-NLS-1$
			@Override
			public void run() {
				// create a buffer to read the stderr output
				InputStreamReader is = new InputStreamReader(process.getErrorStream());
				BufferedReader errReader = new BufferedReader(is);

				try {
					while (true) {
						String line = errReader.readLine();
						if (line != null) {
							errorOutput.add(line);
						} else {
							break;
						}
					}
				} catch (IOException e) {
					// do nothing.
				}
			}
		};

		Thread t2 = new Thread("") { //$NON-NLS-1$
			@Override
			public void run() {
				InputStreamReader is = new InputStreamReader(process.getInputStream());
				BufferedReader outReader = new BufferedReader(is);

				try {
					while (true) {
						String line = outReader.readLine();
						if (line != null) {
							stdOutput.add(line);
						} else {
							break;
						}
					}
				} catch (IOException e) {
					// do nothing.
				}
			}
		};

		t1.start();
		t2.start();

		// it looks like on windows process#waitFor() can return
		// before the thread have filled the arrays, so we wait for both threads
		// and the
		// process itself.
		if (waitForReaders) {
			try {
				t1.join();
			} catch (InterruptedException e) {
			}
			try {
				t2.join();
			} catch (InterruptedException e) {
			}
		}

		// get the return code from the process
		return process.waitFor();
	}
	
}
