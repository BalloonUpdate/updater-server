package top.metime.updater.server.memory;

import java.io.File;
import top.metime.updater.server.tools.MD5;

public class ClientJAR
{
	public File clientJARFile;
	public String clientJARMD5;
	
	public ClientJAR(File jar)
	{
		clientJARFile = jar;
		clientJARMD5 = MD5.getMD5(jar);
	}
	
}
