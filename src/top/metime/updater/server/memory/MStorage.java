package top.metime.updater.server.memory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;

import org.json.JSONObject;
import top.metime.updater.server.tools.MD5;
	
public abstract class MStorage
{
	protected String name;
	
	public String getName()
	{
		return name;
	}
	
	public abstract JSONObject toJSONObject();
	
	 public static class Builder
	{
		private HashMap<String, File> dict = new HashMap<>();
		private MRule mrule;
		
		public Builder(File serv_local, String client_remote, HashSet<String> ignoreFiles)
		{
			MFolder root = new MFolder(serv_local.getName());
			wle(serv_local, root);
			mrule = new MRule(dict, ignoreFiles, root, client_remote);
		}
		
		public Builder(JSONObject obj)
		{
			File serverPath = new File(obj.getString("serverPath"));
			String clientPath = obj.getString("clientPath");
			
			HashSet<String> ignoreFiles = new HashSet<>();
			JSONArray ja = obj.getJSONArray("ignoreFiles");
		
			for(int c=0;c<ja.length();c++)
			{
				ignoreFiles.add(ja.getString(c));
			}

			MFolder root = new MFolder(serverPath.getName());
			wle(serverPath, root);
			mrule = new MRule(dict, ignoreFiles, root, clientPath);
		}
		
		
		private void wle(File readDir, MFolder unrealDir)
		{
			for(File per : readDir.listFiles())
			{
				if(per.isFile())
				{
					String md5 = null;
					
					if(per.length()>0)
					{
						md5 = MD5.getMD5(per);
					}else{
						md5 = "null";
					}
										
					unrealDir.append(new MFile(per.getName(), per.length(), md5));
					dict.put(md5, per);
				}else{
					MFolder sub = new MFolder(per.getName());
					unrealDir.append(sub);
					wle(per, sub);
				}
			}
		}
		
		public MRule getRule()
		{
			return mrule;
		}
	}
}