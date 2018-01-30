package top.metime.updater.server.net.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONArray;
import top.metime.updater.server.memory.MClientJAR;
import top.metime.updater.server.memory.MRule;
import top.metime.updater.server.net.NP;

public class PV0 extends NP
{
	
	private final int delay;
	private final MRule[] rules;
	private final MClientJAR clientJAR;
	
	
	public PV0(DataInputStream netIn, DataOutputStream netOut, int delay, MRule[] rules, MClientJAR clientJAR)
	{
		this.netIn = netIn;
		this.netOut = netOut;
		this.delay = delay;
		this.rules = rules;
		this.clientJAR = clientJAR;
	}

	public void handle() throws IOException
	{
		//向客户端发送最新客户端
		writeFile(clientJAR.clientJARFile);
		
		//写出规则数量
		netOut.writeInt(rules.length);
		
		for(MRule per : rules)
		{
			sendRule(per);
		}
		
	}
	
	
	private void sendRule(MRule rule) throws IOException
	{
		HashMap<String, File> dict = rule.getDictionary();
		
		//发送远程客户端用的路径
		writeString(rule.getRemotePathString());
		
		//发送远程客户端用的文件结构文件摘要
		writeString(rule.getLocalRootDir().toString());
		
		//发送远程客户端用的忽略文件摘要
		writeString(new JSONArray(rule.getIgnoreFiles()).toString());
		

		//询问客户端（还）有没有要传输的文件
		while(readBoolean())
		{
			handleFile(dict);
		}
	}

	private void handleFile(HashMap<String, File> dict) throws IOException
	{
		//接收客户端发回来的Key
		String key = new String(netIn.readUTF());
		
		//表里面寻找Key
		File file = dict.get(key);
		
		//写出文件
		writeFileLimitSpeed(file, delay);
	}
	
}
