package cn.innc11.updater.server.net.protocol;

import java.io.*;
import java.util.HashMap;
import org.json.JSONArray;
import cn.innc11.updater.server.structure.RuleInstance;
import cn.innc11.updater.server.net.Protocol;

public class PV1 extends Protocol
{
	private int delay;
	private RuleInstance[] rules;
	private byte[] clientJAR;

	public PV1(DataInputStream netIn, DataOutputStream netOut, int delay, RuleInstance[] rules, byte[] clientJAR)
	{
		this.netIn = netIn;
		this.netOut = netOut;
		this.delay = delay;
		this.rules = rules;
		this.clientJAR = clientJAR;
	}

	public void handle() throws IOException
	{
		String mainClass = "cn.innc11.updater.client.core.Main";
		writeString(mainClass);

		netOut.writeLong(clientJAR.length); // 写出文件长度

		writeByteArrays(clientJAR); // 向客户端发送最新客户端
		
		netOut.writeInt(rules.length); // 写出规则数量
		
		for(RuleInstance per : rules)
		{
			sendRule(per);
		}
	}

	private void sendRule(RuleInstance rule) throws IOException
	{
		HashMap<String, File> dict = rule.dict;

		writeString(rule.remotePath); // 发送远程客户端用的路径
		writeString(rule.localFolder.toString()); // 发送远程客户端用的文件结构文件摘要
		writeString(new JSONArray(rule.ignoreFiles).toString()); // 发送远程客户端用的忽略文件摘要

		while(readBoolean()) // 询问客户端（还）有没有要传输的文件
		{
			handleFile(dict);
		}
	}

	private void handleFile(HashMap<String, File> dict) throws IOException
	{
		String key = new String(netIn.readUTF()); // 接收客户端发回来的Key
		File file = dict.get(key); // 表里面寻找Key
		writeFileLimitSpeed(file, delay); // 写出文件
	}
	
}
