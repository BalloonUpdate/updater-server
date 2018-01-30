package top.metime.updater.server.memory;

import java.io.File;
import top.metime.updater.server.tools.MD5;

public class MClientJAR
{
	public File clientJARFile;
	public String clientJARMD5;
	
	public MClientJAR(File jar)
	{
		clientJARFile = jar;
		clientJARMD5 = MD5.getMD5(jar);
	}
	
}
