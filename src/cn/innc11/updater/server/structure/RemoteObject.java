package cn.innc11.updater.server.structure;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;
import cn.innc11.updater.server.tools.MD5;
	
public abstract class RemoteObject
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
		private RuleInstance mrule;
		
		public Builder(File serv_local, String client_remote, HashSet<String> ignoreFiles)
		{
			RemoteFolder root = new RemoteFolder(serv_local.getName());
			wle(serv_local, root);
			mrule = new RuleInstance(dict, ignoreFiles, root, client_remote);
		}
		
		public Builder(JSONObject obj)
		{
			File localPath = new File(obj.getString("serverPath"));
			String remotePath = obj.getString("clientPath");
			
			HashSet<String> ignoreFiles = new HashSet<>();
			JSONArray ja = obj.getJSONArray("ignoreFiles");
		
			for(int c=0;c<ja.length();c++)
			{
				ignoreFiles.add(ja.getString(c));
			}

			RemoteFolder root = new RemoteFolder(localPath.getName());
			wle(localPath, root);
			mrule = new RuleInstance(dict, ignoreFiles, root, remotePath);
		}
		
		
		private void wle(File RFile, RemoteFolder VFile)
		{
			for(File per : RFile.listFiles())
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
										
					VFile.append(new RemoteFile(per.getName(), per.length(), md5));
					dict.put(md5, per);
				}else{
					RemoteFolder sub = new RemoteFolder(per.getName());
					VFile.append(sub);
					wle(per, sub);
				}
			}
		}
		
		public RuleInstance build()
		{
			return mrule;
		}
	}
}
