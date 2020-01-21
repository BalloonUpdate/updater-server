package cn.innc11.updater.server.structure;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class RuleInstance
{
	public HashMap<String, File> dict;
	public HashSet<String> ignoreFiles;
	public final MFolder localFolder;
	public final String remotePath;
	
	public RuleInstance(HashMap<String, File> dict, HashSet<String> ignoreFiles, MFolder localFolder, String remotePath)
	{
		this.dict = dict;
		this.ignoreFiles = ignoreFiles;
		this.localFolder = localFolder;
		this.remotePath = remotePath;
	}
	
}
