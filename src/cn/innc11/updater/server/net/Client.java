package cn.innc11.updater.server.net;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;

import cn.innc11.updater.server.structure.RuleInstance;

public class Client implements Runnable
{
	private final static byte[] PROTOCOL_HEAD_ACK = {0x23, 0x04, 0x01, 0x34, 0x51, 0x33, 0x35, 0x18};
	
	private Socket socket;
	private int delay;
	private RuleInstance[] crs;
	private byte[] clientJAR;

	private ClientDisconnectedCallback clientDisconnectedListener;
	
	private DataInputStream netIn;
	private DataOutputStream netOut;
	
	public Client(Socket socket, int delay, RuleInstance[] crs, byte[] clientJAR, ClientDisconnectedCallback cdl)
	{
		this.delay = delay;
		this.crs = crs;
		this.socket= socket;
		this.clientJAR = clientJAR;

		clientDisconnectedListener = cdl;
	}
	
	@Override
	public void run()
	{
		try
		{
			socket.setSoTimeout(120000);
			netIn = new DataInputStream(socket.getInputStream());
			netOut = new DataOutputStream(socket.getOutputStream());
			
			ACK(PROTOCOL_HEAD_ACK);//验证协议
			
			int clientProtocolVer = readInt(); // 获取客户端协议版本
			
			if(isSupportProtocol(clientProtocolVer)) // 如果支持此协议
			{
				//返回客户端本服务端可以处理
				writeBoolean(true);
				
				try {
					Class<?> pv = Class.forName(Service.NET_PROTOCOL_HANDLER_PACKAGE_NAME+".PV"+clientProtocolVer);
					Constructor<?> cons = pv.getDeclaredConstructor(DataInputStream.class, DataOutputStream.class, int.class, RuleInstance[].class, byte[].class);
					pv.getDeclaredMethod("handle").invoke(cons.newInstance(netIn, netOut, delay, crs, clientJAR));
				}
				catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
				{
					ex.printStackTrace();
					Throwable t = ex.getCause();
					if(!(t instanceof IOException))
					{
						System.exit(1);
					}
				}
				
			}else{
				writeBoolean(false); // 返回客户端本服务端无法处理
				writeString(Arrays.toString(Service.NET_SUPPORT_PROTOCOL_VERSIONS)); // 告诉客户端服务端的协议版本
			}
			
		}
		catch (IOException e){e.printStackTrace();}
		finally
		{
			try {
				socket.close();
			} 
			catch (IOException e1) {e1.printStackTrace();}
			finally
			{
				if(clientDisconnectedListener!=null) // 客户端
				{
					String hostAdd = socket.getInetAddress().getHostAddress();
					int hostPort = socket.getPort();
					
					clientDisconnectedListener.onClientDisconnected(hostAdd, hostPort, this);
				}
			}
		}

	}

	
	private boolean isSupportProtocol(int clientVer)
	{
		for(int per : Service.NET_SUPPORT_PROTOCOL_VERSIONS)
		{
			if(clientVer==per) return true;
		}
		return false;
	}
	
	
	public boolean ACK(byte[] ack) throws IOException
	{
		netOut.write(ack);
		byte[] inres = new byte[ack.length];
		netIn.read(inres);
		return Arrays.equals(inres, ack);
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
	public void writeString(String str) throws IOException
	{
		final int maxTransportLength = 1024;
		
		//告诉客户端字符串的长度
		netOut.writeInt(str.length());
		
		int at = (int)(str.length() / maxTransportLength);
		int bt = (int)(str.length() % maxTransportLength);
		
		for (int c = 0; c < at; c++)
		{
			netOut.write(str.substring(c*maxTransportLength, (c+1)*maxTransportLength).getBytes(Charset.forName("UTF-8")));
		}
		netOut.write(str.substring(at*maxTransportLength, str.length()).getBytes(Charset.forName("UTF-8")));
	}
	
	//completed
	public String readString() throws IOException
	{
		final int maxTransportLength = 1024;
		
		//接受服务端发来的字符串长度
		int stringLength = netIn.readInt();
		
		int at = (int)(stringLength / maxTransportLength);
		int bt = (int)(stringLength % maxTransportLength);
		
		StringBuilder sb = new StringBuilder();
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

	public interface ClientDisconnectedCallback
	{
		void onClientDisconnected(String host, int port, Object source);
	}

}
