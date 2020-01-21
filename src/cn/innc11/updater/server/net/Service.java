package cn.innc11.updater.server.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.innc11.updater.server.excption.FileUnableParseExcption;
import cn.innc11.updater.server.excption.InvalidRuleLocalPathException;
import cn.innc11.updater.server.Config;
import cn.innc11.updater.server.structure.RuleInstance;

public class Service implements Runnable // 该类是一个对ThreadPoolExecutor类和ServerSocket类的封装，以异步方式工作
{
	public static final int[] NET_SUPPORT_PROTOCOL_VERSIONS = {0, 1}; // 支持的协议版本
	public static final String NET_PROTOCOL_HANDLER_PACKAGE_NAME = "cn.innc11.updater.server.net.protocol"; // 协议处理类的所在包
	public static final int OPCODE_SOCKET_SAFE_CLOSED = 0; // 操作码：套接字已经安全关闭
	public static final int OPCODE_SOCKET_UNEXPECTED_CLOSED = 1; // 操作码：套接字被意外关闭
	
	private ThreadPoolExecutor tpool;//线程池
	private ServerSocket serverSocket;//监听
	
	private int delay = 0;
	private int port;
	private RuleInstance[] rules;
	private byte[] clientJAR;
	private int maxOnlineClients;

	private boolean willExit = false;
	
	//在线的客户端列表
	private LinkedList<Client> runningClient = new LinkedList<>();

	public Service()
	{
		Thread.currentThread().setName("MainService");
	}


	@Override
	public void run()
	{
		while(true)
		{
			try 
			{
				if(willExit) return;

				if(tpool==null||serverSocket==null)
				{
					Thread.sleep(1000);
				}else{
					Socket socket = serverSocket.accept();

					Client client = new Client(socket, delay, rules, clientJAR, (String host, int port, Object source)->
					{
						//System.out.println(host+":"+port+"断开了连接！");
						System.out.println(host+":"+port);
						runningClient.remove(source);
					});
					
					runningClient.add(client);
					tpool.execute(client);
				}
			}
			catch (IOException e) 
			{
				if(!e.getMessage().equalsIgnoreCase("socket closed"))
				{
					e.printStackTrace();
				}
			} 
			catch (InterruptedException e) 
			{
				if(!e.getMessage().equals("sleep interrupted"))
				{
					e.printStackTrace();
				}
			}
		}
		
	}

	public void load(int port, int maxDownstreamRate, int maxOnlineClients, RuleInstance[] rules, byte[] clientJAR)
	{
		this.port = port;
		this.delay = maxDownstreamRate<0?0:1000/(maxDownstreamRate/4);
		this.maxOnlineClients = maxOnlineClients;
		this.rules = rules;
		this.clientJAR = clientJAR;
	}

	public boolean startService()
	{
		if(rules.length==0) return false;//如果没有规则可以用
		
		try {
			tpool = new ThreadPoolExecutor(maxOnlineClients, maxOnlineClients, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
			serverSocket = new ServerSocket(port);
			
			System.out.println("服务已启动！");
			
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void stopService()
	{
		try	{
			//强制关掉线程池
			if(tpool!=null)tpool.shutdownNow();
			
			//关掉ServerSocket监听
			if(serverSocket!=null)serverSocket.close();
			
			tpool=null;
			serverSocket=null;

			System.out.println("服务已停止，操作码："+OPCODE_SOCKET_SAFE_CLOSED+"，原因"+"正常关闭");
			forceExit();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public boolean isRunned()
	{
		return tpool!=null&&serverSocket!=null&&!tpool.isShutdown()&&serverSocket.isBound()&&!serverSocket.isClosed();
	}
	
	public Client[] getClients()
	{
		return runningClient.toArray(new Client[0]);
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void forceExit()
	{
		willExit=true;
		
		try {
			//强制关掉线程池
			if(tpool!=null)tpool.shutdownNow();
			
			//关掉ServerSocket监听
			if(serverSocket!=null)serverSocket.close();
			
			tpool=null;
			serverSocket=null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

/*
	public interface ConfigGeter
	{
		Config getMConfig();
	}

	public interface RulesGeter
	{
		HashSet<RuleInstance> getRules() throws FileUnableParseExcption, InvalidRuleLocalPathException;
	}

 */
}
