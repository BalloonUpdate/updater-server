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
import top.metime.updater.server.callback.ServiceEventHandleCallback;
import top.metime.updater.server.callback.ThrowExceptionCallback;
import top.metime.updater.server.memory.ClientJAR;
import top.metime.updater.server.memory.MRule;

/**
 * 该类是一个对ThreadPoolExecutor类和ServerSocket类的封装，以异步方式工作
 * 
 * @author innc
 */
public class Service implements Runnable
{
	/**
	 * 支持的协议版本
	 */
	public static final int[] NET_PROTOCOL_SUPPORT_VERSIONS = {0, 1};

	/**
	 * 协议处理类的所在包
	 */
	public static final String NET_PROTOCOL_HANDLER_PACKAGE_NAME = "top.metime.updater.server.net.protocol";
	
	/**
	 * 操作码：套接字已经安全关闭
	 */
	public static final int OPCODE_SOCKET_SAFE_CLOSED = 0;

	/**
	 *  操作码：套接字被意外关闭
	 */
	public static final int OPCODE_SOCKET_UNEXPECTED_CLOSED = 1;
	
	private ThreadPoolExecutor tpool;//线程池
	private ServerSocket serverSocket;//监听用的ServerSocket
	
	private int delay = 0;
	private int port;
	private MRule[] rules;
	private ClientJAR clientJAR;
	private int maxOnlineClients;
	
	private ServiceEventHandleCallback eventCallback;
	private ClientConnectedCallback clientConnectedCallback;
	private ClientDisconnectedCallback clientDisconnectedCallback;
	private ThrowExceptionCallback throwExceptionCallback;
	
	//在线的客户端列表
	private LinkedList<Client> runningClient = new LinkedList<>();
	
	/**
	 * 构造方法
	 */
	public Service()
	{
		//设置线程的名字
		Thread.currentThread().setName("MainService");
	}
	
	
	// 事件回调

	/**
	 * 设置 Service的事件 处理回调
	 *
	 * @param callback 回调对象
	 */
	public void setServiceEventHandleCallback(ServiceEventHandleCallback callback)
	{
		eventCallback = callback;
	}
	
	/**
	 * 设置 Client连接上的事件 处理回调
	 *
	 * @param callback 回调对象
	 */
	public void setClientConnectedCallback(ClientConnectedCallback callback)
	{
		clientConnectedCallback = callback;
	}
	
	/**
	 * 设置 Client断开的事件 处理回调
	 *
	 * @param callback 回调对象
	 */
	public void setClientDisconnectedCallback(ClientDisconnectedCallback callback)
	{
		clientDisconnectedCallback = callback;
	}
	
	/**
	 * 设置 抛出异常事件 处理回调
	 *
	 * @param callback 回调对象
	 */
	public void setThrowExceptionCallback(ThrowExceptionCallback callback)
	{
		throwExceptionCallback = callback;
	}
	

	private boolean willExit = false;
	
	//主方法
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

	/**
	 * 加载规则
	 *
	 * @param port 监听的端口
	 * @param maxDownstreamRate 最大下行速率
	 * @param maxOnlineClients 最大同时在线客户端
	 * @param rules 规则对象（Array）
	 * @param clientJAR 客户端文件对象
	 */
	public void load(int port, int maxDownstreamRate, int maxOnlineClients, MRule[] rules, ClientJAR clientJAR)
	{
		this.port = port;
		this.delay = maxDownstreamRate<0?0:1000/(maxDownstreamRate/4);
		this.maxOnlineClients = maxOnlineClients;
		this.rules = rules;
		this.clientJAR = clientJAR;
	}
	
	/**
	 * 请求启动服务（初始化线程池、监听端口）
	 *
	 * @return 当成功启动时返回true，失败时返回false
	 */
	public boolean startService() 
	{
		if(rules.length==0) return false;//如果没有规则可以用
		
		try 
		{
			//初始化线程池
			tpool = new ThreadPoolExecutor(maxOnlineClients, maxOnlineClients, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
			
			//初始化ServerSocket
			serverSocket = new ServerSocket(port);
			
			//触发[已启动]事件
			eventCallback.onServiceStarted(port);
			
			return true;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			throwExceptionCallback.onThrowException(e);//触发[抛出异常]事件
		}
		
		return false;
	}
	
	/**
	 * 请求停止服务
	 */
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
			
			eventCallback.onServiceStopped(OPCODE_SOCKET_SAFE_CLOSED, "正常关闭");//触发[已停止]事件
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throwExceptionCallback.onThrowException(e);//触发[抛出异常]事件
		}
	}
	
	
	// 控制 API

	/**
	 * 获取服务是否在运行时的状态
	 *
	 * @return 当服务在运行（监听端口）时，返回true，不在运行时返回false
	 */
	public boolean isRunned()
	{
		return tpool!=null&&serverSocket!=null&&!tpool.isShutdown()&&serverSocket.isBound()&&!serverSocket.isClosed();
	}
	
	/**
	 * 获取当前在线的客户端对象（Array）
	 *
	 * @return 当前在线的客户端对象Array
	 */
	public Client[] getClients()
	{
		return runningClient.toArray(new Client[0]);
	}
	
	/**
	 * 获取监听的端口（不随运行状态改变）
	 *
	 * @return 监听的端口
	 */
	public int getPort()
	{
		return port;
	}
	
	/**
	 * 强制关闭并退出服务（不会触发成功关闭事件）
	 */
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
