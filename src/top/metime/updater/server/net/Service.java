package top.metime.updater.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import top.metime.updater.server.callback.ClientConnectedCallback;
import top.metime.updater.server.callback.ClientDisconnectedCallback;

import top.metime.updater.server.callback.ThrowExceptionCallback;
import top.metime.updater.server.callback.ServiceEventHandleCallback;
import top.metime.updater.server.memory.MClientJAR;
import top.metime.updater.server.memory.MRule;

public class Service implements Runnable
{
	//通信协议版本
	public static final int[] NET_PROTOCOL_SUPPORT_VERSIONS = {0};
	public static final String NET_PROTOCOL_HANDLER_PACKAGE_NAME = "top.metime.updater.server.net.protocol";
	
	
	public static final int OPCODE_SOCKET_CLOSE_SAFE = 0;
	public static final int OPCODE_SOCKET_CLOSE_UNEXPECTED = 1;
	
	private ThreadPoolExecutor tpool;//线程池
	private ServerSocket serverSocket;//监听ServerSocket
	
	private int delay = 0;
	private int port;
	private MRule[] rules;
	private MClientJAR clientJAR;
	private int maxOnlineClients;
	
	private ServiceEventHandleCallback eventCallback;
	private ClientConnectedCallback clientConnectedCallback;
	private ClientDisconnectedCallback clientDisconnectedCallback;
	private ThrowExceptionCallback throwExceptionCallback;
	
	//在线的客户端列表
	private LinkedList<Client> runningClient = new LinkedList<>();
	
	
	public Service()
	{
		Thread.currentThread().setName("MainService");//设置线程的名字
	}
	
	
	// 事件回调
	public void setServiceEventHandleCallback(ServiceEventHandleCallback callback)
	{
		eventCallback = callback;
	}
	
	public void setClientConnectedCallback(ClientConnectedCallback callback)
	{
		clientConnectedCallback = callback;
	}
	
	public void setClientDisconnectedCallback(ClientDisconnectedCallback callback)
	{
		clientDisconnectedCallback = callback;
	}
	
	public void setThrowExceptionListener(ThrowExceptionCallback callback)
	{
		throwExceptionCallback = callback;
	}
	

	//主方法
	private boolean willExit = false;
	@Override
	public void run()
	{
		while(true)
		{
			try 
			{
				if(willExit) return;
				if(tpool==null||serverSocket==null)/*如果未初始化*/ Thread.sleep(1000);
				else
				{
					Socket socket = serverSocket.accept();
					
					Client client = new Client(socket, delay, rules, clientJAR, (String host, int port, Object source)->
					{
						clientDisconnectedCallback.onClientDisconnected(host, port, source);//向上调用
						runningClient.remove(source);
					});
					
					runningClient.add(client);
					tpool.execute(client);
					clientConnectedCallback.onClientConnected(socket.getInetAddress().getHostAddress(), socket.getPort());
				}
			}
			catch (IOException e) 
			{
				if(!e.getMessage().equalsIgnoreCase("socket closed"))
				{
					e.printStackTrace();
					throwExceptionCallback.onThrowException(e);//触发[抛出异常]事件
				}
			} 
			catch (InterruptedException e) 
			{
				if(!e.getMessage().equals("sleep interrupted"))
				{
					e.printStackTrace();
					throwExceptionCallback.onThrowException(e);//触发[抛出异常]事件
				}
			}
		}
		
	}
	
	
	// 工作模式 API
	public void load(int port, int maxDownstreamRate, int maxOnlineClients, MRule[] rules, MClientJAR clientJAR)
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
		
		
			try 
			{
				//初始化线程池
				tpool = new ThreadPoolExecutor(maxOnlineClients, maxOnlineClients, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
				
				;//初始化ServerSocket
				serverSocket = new ServerSocket(port);
				
				//触发[已启动]事件
				eventCallback.onServiceStarted(port);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				throwExceptionCallback.onThrowException(e);//触发[抛出异常]事件
			}
		
		
		return true;
	}
	
	public void stopService()
	{
		try
		{
			//强制关掉线程池
			if(tpool!=null)tpool.shutdownNow();
			
			//关掉ServerSocket监听
			if(serverSocket!=null)serverSocket.close();
			
			tpool=null;
			serverSocket=null;
			
			eventCallback.onServiceStopped(OPCODE_SOCKET_CLOSE_SAFE, "正常关闭");//触发[已停止]事件
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throwExceptionCallback.onThrowException(e);//触发[抛出异常]事件
		}
	}
	
	
	// 控制 API
	public boolean isRunned()
	{
		return tpool!=null&&serverSocket!=null&&!tpool.isShutdown()&&serverSocket.isBound()&&!serverSocket.isClosed();
	}
	
	public Client[] getClients()
	{
		return runningClient.toArray(new Client[1]);
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void exit()
	{
		willExit=true;
		
		try
		{
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
			throwExceptionCallback.onThrowException(e);//触发[抛出异常]事件
		}
	}
}
