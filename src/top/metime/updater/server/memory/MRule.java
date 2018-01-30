package top.metime.updater.server.memory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class MRule 
{
	private HashMap<String, File> dict;
	private HashSet<String> ignoreFiles;
	private final MFolder localFolder;
	private final String remotePath;
	
	public MRule(HashMap<String, File> dict, HashSet<String> ignoreFiles, MFolder localFolder, String remotePath)
	{
		this.dict = dict;
		this.ignoreFiles = ignoreFiles;
		this.localFolder = localFolder;
		this.remotePath = remotePath;
	}
	
	public HashMap<String, File> getDictionary()
	{
		return dict;
	}
	
	public HashSet<String> getIgnoreFiles()
	{
		return ignoreFiles;
	}
	
	public MFolder getLocalRootDir()
	{
		return localFolder;
	}
	
	public String getRemotePathString()
	{
		return remotePath;
	}
}
