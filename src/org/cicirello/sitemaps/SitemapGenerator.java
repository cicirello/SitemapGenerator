package org.cicirello.sitemaps;
/*
 * Sitemap Generator:
 *
 * Copyright (C) 2017 Vincent A. Cicirello.
 * http://www.cicirello.org/
 *
 * Command line utility for generating an xml sitemap for a website whose code is
 * maintained via git.  It walks the directory hierarchy of the site, and uses the
 * command line git command to find the last commit dates for use as last modification
 * dates in the sitemap.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.*;
import java.io.*;

/**
 *
 * Command line utility for generating an xml sitemap for a website whose code is
 * maintained via git.  It walks the directory hierarchy of the site, and uses the
 * command line git command to find the last commit dates for use as last modification
 * dates in the sitemap.
 *
 * Configure the sitemap generator via the config.txt file, which supports the following:
 *   GIT_ROOT: C:\ FullPathToLocalGitRepository\ user.github.io
 *   GIT_EXEC: C:\FullPathToGitCommand\cmd\git
 *   PAGE_ROOT: http://www.YourWebAddressHere.org
 *   INCLUDE_EXT: html pdf [And any other file extensions you want in the sitemap space separated]
 *   EXCLUDE_DIR: images [Space separated list of any directories you want to exclude from sitemap, images is just an example]
 *   EXCLUDE_FILE: 404.html [Space separated list of any specific files you want excluded from sitemap]
 *
 * @author Vincent A. Cicirello
 * @version 8.17.2017
*/
public class SitemapGenerator {
	
	private static String root;  
	private static String git; 
	private static String pageRoot; 
	private final static HashSet<String> includeTheseExtensions = new HashSet<String>(); 
	private final static HashSet<String> excludeTheseDirs  = new HashSet<String>();
	private final static ArrayList<String> excludeTheseFiles  = new ArrayList<String>();
	private static File workingDir; 
	
	public static void main(String[] args) {
		outputCopyrightNotice();
		readConfig();
		
		System.out.println("Generating your sitemap.  This may take a while depending on size of site....");
		
		ArrayList<String> urls = new ArrayList<String>(); 
		HashMap<String,String> lastMod = new HashMap<String,String>(); 
		
		File[] files = new File(root).listFiles();
		getFiles(files, urls, lastMod);
		
		Collections.sort(urls);
		
		
		PrintWriter out = null;
		String sitemap = root + "\\sitemap.xml";
		try {
			out = new PrintWriter(sitemap);
		} catch (FileNotFoundException ex) {
			System.out.println("Output fle not found: " + ex);
			System.exit(0);
		}
		
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		for (String e : urls) {
			out.println("<url>");
			out.println("<loc>" + e + "</loc>");
			String last = lastMod.get(e);
			if (e!=null) out.println("<lastmod>" + last + "</lastmod>");
			out.println("</url>");
		}
		out.println("</urlset>");
		out.flush();
		out.close();
		
		System.out.println("Complete!");
	}
	
	private static void readConfig() {
		String configFileName = "config.txt";
		File f = new File(configFileName);
		if (f.exists()) {
			Scanner config = null;
		
			try {
				config = new Scanner(f);
			} catch (FileNotFoundException ex) {
				System.out.println(configFileName + " not found: " + ex);
				System.exit(0);
			}
			while (config.hasNextLine()) {
				String line = config.nextLine();
				Scanner lineScan = new Scanner(line);
				String label = lineScan.next();
				if (label.equalsIgnoreCase("GIT_ROOT:")) {
					root = lineScan.nextLine().trim();
					workingDir = new File(root);
				} else if (label.equalsIgnoreCase("GIT_EXEC:")) {
					git = lineScan.nextLine().trim();
				} else if (label.equalsIgnoreCase("PAGE_ROOT:")) {
					pageRoot = lineScan.nextLine().trim();
				} else if (label.equalsIgnoreCase("INCLUDE_EXT:")) {
					while (lineScan.hasNext()) {
						includeTheseExtensions.add(lineScan.next().trim());
					}
				} else if (label.equalsIgnoreCase("EXCLUDE_DIR:")) {
					while (lineScan.hasNext()) {
						excludeTheseDirs.add(lineScan.next().trim());
					}
				} else if (label.equalsIgnoreCase("EXCLUDE_FILE:")) {
					while (lineScan.hasNext()) {
						excludeTheseFiles.add(lineScan.next().trim());
					}
				}
			}
			config.close();
		} else {
			System.out.println("Configuration file, config.txt, does not exist. Exiting.");
			System.exit(0);
		}
	}
	
	
	private static void getFiles(File[] files, ArrayList<String> urls, HashMap<String,String> lastMod) {
		for (File file : files) {
			if (file.isDirectory()) {
				if (!excludeTheseDirs.contains(file.getName())) getFiles(file.listFiles(), urls, lastMod);
			} else {
				String fileName = file.getPath();
				if (doExcludeFile(fileName)) continue;
				int i = fileName.lastIndexOf(".");
				String ext = "";
				if (i > 0) ext = fileName.substring(i+1);
				if (includeTheseExtensions.contains(ext.toLowerCase())) {
					String last = "";
					try {
						String command = git + " log -1 --format=%ci " + fileName; 
						Process cmdProc = Runtime.getRuntime().exec(command, null, workingDir);
						BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
						String line;
						while ((line = stdoutReader.readLine()) != null) {
							int j = line.indexOf(" ");
							line = line.substring(0,j) + "T" + line.substring(j+1);
							j = line.indexOf(" ");
							line = line.substring(0,j) + line.substring(j+1,j+4) + ":" + line.substring(j+4,j+6);
							last = line;
						}
						int retValue = cmdProc.exitValue();
					} catch (IOException e) {
						System.out.println("Exception while getting last commit date: " + e);
					}
					fileName = fileName.replace(root, pageRoot).replace("\\","/").replace("index.html", "");
					urls.add(fileName);
					if (last.length() > 0) lastMod.put(fileName,last);
				}
			}
		}
	}
	
	private static boolean doExcludeFile(String filenameWithPath) {
		for (String exclude : excludeTheseFiles) {
			if (filenameWithPath.contains(exclude)) return true;
		}
		return false;
	}
	
	private static void outputCopyrightNotice() {
		System.out.println("\nSitemap Generator, Copyright (C) 2017 Vincent A. Cicirello\n");
		System.out.println("The source code is available within the jar file or also from");
		System.out.println("http://www.cicirello.org/\n");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY; This is free software,");
		System.out.println("and you are welcome to redistribute it under certain conditions.");
		System.out.println("This program is distributed in the hope that it will be useful,");
		System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
		System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		System.out.println("GNU General Public License for more details.");
		System.out.println("https://www.gnu.org/licenses/gpl-3.0.html\n");
	}
	
	
	
}