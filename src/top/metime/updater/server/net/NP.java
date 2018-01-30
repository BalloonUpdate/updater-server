package top.metime.updater.server.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class NP
{
	protected DataInputStream netIn;
	protected DataOutputStream netOut;
	
	//completed
	public void writeFileLimitSpeed(File file, int delay) throws IOException
	{
		//写出文件长度
		try (FileInputStream fileIn = new FileInputStream(file))
		{
			//写出文件长度
			netOut.writeLong(file.length());
			
			byte[] buffer = new byte[1024*4];
			int len = 0;
			while((len = fileIn.read(buffer))!=-1)
			{
				//速度限制
				try{if(delay>0) Thread.sleep(delay);}catch (InterruptedException ex) {Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);}
				
				netOut.write(buffer, 0, len);
			}
		}
	}
	
	//completed
	public void writeFile(File file) throws IOException
	{
		//写出文件长度
		try (FileInputStream fileIn = new FileInputStream(file))
		{
			//写出文件长度
			netOut.writeLong(file.length());
			
			byte[] buffer = new byte[1024*4];
			int len = 0;
			while((len = fileIn.read(buffer))!=-1)
			{
				netOut.write(buffer, 0, len);
			}
		}
	}
	
	//completed
	public void readFile(File file, long length) throws IOException
	{
		file.createNewFile();
		
		if(length!=0)
		{
			try (FileOutputStream fos = new FileOutputStream(file))
			{
				byte[] buf = new byte[4096];
				int rcount = (int)(length / buf.length);
				int dyv = (int)(length % buf.length);
				int cp = 0;
				for (int c = 0; c < rcount; c++)
				{
					netIn.readFully(buf);
					
					fos.write(buf, 0, buf.length);
				}
				
				for (int c = 0; c < dyv; c++)
				{
					fos.write(netIn.readByte());
				}
			}
		}
	}
	
	//completed
	public boolean Ack(byte[] ack) throws IOException
	{
		netOut.write(ack);
		byte[] inres = new byte[ack.length];
		netIn.read(inres);
		return Arrays.equals(inres, ack);
	}

	//completed
	public void writeString(String str) throws IOException
	{
		final int max = 1024;
		
		byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
		
		//告诉客户端数组的长度
		netOut.writeInt(bytes.length);
		int at = (int)(bytes.length / max);

		int bt = (int)(bytes.length % max);
		
		for (int c = 0; c < at; c++)
		{
			netOut.write(bytes, c*max, max);
		}
		netOut.write(bytes, at*max, bt);
	}
	
	//completed
	public String readString() throws IOException
	{
		final int maxTransportLength = 1024;
		
		StringBuilder sb = new StringBuilder();
		
		//接受服务端发来的数组的长度
		int bytesLength = netIn.readInt();
		
		int at = (int)(bytesLength / maxTransportLength);
		int bt = (int)(bytesLength % maxTransportLength);
		
		byte[] buffer = new byte[maxTransportLength];
		for (int c = 0; c < at; c++)
		{
			netIn.readFully(buffer);
			sb.append(new String(buffer, Charset.forName("UTF-8")));
		}
		
		netIn.readFully(buffer, 0, bt);
		
		sb.append(new String(buffer, 0, bt, Charset.forName("UTF-8")));
		
		return sb.toString();
	}

	//completed
	public void writeInt(int value) throws IOException
	{
		netOut.writeInt(value);
	}
	
	//completed
	public int readInt() throws IOException
	{
		return netIn.readInt();
	}
	
	//completed
	public void writeBoolean(boolean bool) throws IOException
	{
		netOut.writeBoolean(bool);
	}
	
	//completed
	public boolean readBoolean() throws IOException
	{
		return netIn.readBoolean();
	}

	//completed
	public void writeBytes(byte[] bytes) throws IOException
	{
		final int max = 1024;
		
		//告诉客户端数组的长度
		netOut.writeInt(bytes.length);
		
		int at = (int)(bytes.length / max);
		int bt = (int)(bytes.length % max);
		
		for (int c = 0; c < at; c++)
		{
			netOut.write(bytes, c*max, (c+1)*max);
		}
		netOut.write(bytes, at*max, bt);
	}

	//completed
	public byte[] readByteArray() throws IOException
	{
		final int max = 1024;
		
		//接受服务端发来的数组的长度
		int bytesLength = netIn.readInt();
		
		int at = (int)(bytesLength / max);
		int bt = (int)(bytesLength % max);
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream(bytesLength);
		
		byte[] buffer = new byte[max];
		for (int c = 0; c < at; c++)
		{
			netIn.readFully(buffer);
			bout.write(buffer);
		}
		
		netIn.readFully(buffer, 0, bt);
		bout.write(buffer, 0, bt);
		
		return bout.toByteArray();
	}
}
