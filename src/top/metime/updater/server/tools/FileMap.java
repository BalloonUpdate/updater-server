package top.metime.updater.server.tools;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class FileMap implements Serializable
{
	private static final long serialVersionUID = 1L;
	private HashMap<String, File> dict;
	
	public FileMap()
	{
		dict = new HashMap<>();
	}
	
	public boolean containsKey(String key)
	{
		return dict.containsKey(key);
	}
	
	public void addEntry(String key, File file)
	{
		dict.put(key, file);
	}
	
	public File getFile(String key)
	{
		return dict.get(key);
	}
	
	public void clearAll()
	{
		dict = null;
		dict = new HashMap<>();
	}
}
