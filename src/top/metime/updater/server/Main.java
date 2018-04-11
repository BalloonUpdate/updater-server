package top.metime.updater.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import top.metime.updater.server.callback.ServiceEventHandleCallback;
import top.metime.updater.server.config.ConfigGeter;
import top.metime.updater.server.config.RulesGeter;
import top.metime.updater.server.excption.FileUnableParseExcption;
import top.metime.updater.server.excption.InvalidRuleLocalPathException;
import top.metime.updater.server.init.TrayUI;
import top.metime.updater.server.memory.ClientJAR;
import top.metime.updater.server.memory.MConfig;
import top.metime.updater.server.memory.MRule;
import top.metime.updater.server.net.Service;
import top.metime.updater.server.tools.RulesLoader;

/**
 * 程序的入口类
 * @author innc
 */
public class Main implements ConfigGeter, RulesGeter
{
	private final String PARAMETER_NOUI = "noui";
	private final String propertiesFileName = "serverConfig.properties";
	private final String rulesFileName = "serverRules.json";
	
	private Thread serviceThread;
	private Service service;
	
	
	/**
	 * 程序的入口方法
	 * @param args Class参数
	 */
	public static void main(String[] args) throws InterruptedException
	{
		Main m = new Main();
		m.main2(args);
	}
	
	private void main2(String[] args) throws InterruptedException
	{
		service = new Service();
		serviceThread = new Thread(service);
		serviceThread.start();//启动线程
		
		//Thread.sleep(1000);
		
		//如果参数数量大于0 且 第一个参数是PARAMETER_NOUI
		if(args.length>0&&args[0].equalsIgnoreCase(PARAMETER_NOUI))
		{
			initCommandLine(service, this, this);
		}else{
			new TrayUI(service, this, this);
		}
		
	}

	@Override
	public MConfig getMConfig() 
	{
		MConfig mconfig = new MConfig();
		
		try(FileInputStream fileIs = new FileInputStream(propertiesFileName))
		{
			mconfig.load(fileIs);
		}
		catch (IOException | IllegalArgumentException | IllegalAccessException ex) 
		{
			ex.printStackTrace();
			service.stopService();
			service.exit();
		} 

		return  mconfig;
	}

	/**
	 * 获取一个规则集合
	 * 
	 * @return 返回一个MRule的集合，可能因为配置文件什么都没有写而返回空集合
	 * @throws FileUnableParseExcption 文件格式错误，无法读取
	 * @throws top.metime.updater.server.excption.InvalidRuleLocalPathException localPath不存在
	 */
	@Override
	public HashSet<MRule> getRules() throws FileUnableParseExcption, InvalidRuleLocalPathException 
	{
		HashSet<MRule> rules = null;
		try(FileInputStream fileIs = new FileInputStream(rulesFileName))
		{
			//可能因为配置文件什么都没有写而返回空集合
			rules = RulesLoader.loadRules(fileIs, "./"+rulesFileName);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			service.stopService();
			service.exit();
		}
		
		return rules;
	}
	
	
	private void initCommandLine(Service service, ConfigGeter configGeter, RulesGeter rulesGeter)
	{
		//设置 客户端连接后 的处理回调
		service.setClientConnectedCallback((String host, int port) ->
		{
			System.out.println(host+":"+port+"连接上来了！");
		});
		
		//设置 客户端断开后 的处理回调
		service.setClientDisconnectedCallback((String host, int port, Object source) ->
		{
			System.out.println(host+":"+port+"断开了连接！");
		});

		//设置 服务触发的事件 的处理回调
		service.setServiceEventHandleCallback(new ServiceEventHandleCallback()
		{
			@Override
			/**
			 * 当服务启动后
			 * 
			 * @param port 端口
			 */
			public void onServiceStarted(int port)
			{
				System.out.println("服务已启动！");
			}

			@Override
			/**
			 * 当服务停止后
			 * 
			 * @param opcode 操作码
			 * @param reson 原因
			 */
			public void onServiceStopped(int opcode, String reson)
			{
				System.out.println("服务已停止，操作码："+opcode+"，原因"+reson);
				service.exit();
			}

		});

		//当服务抛出异常
		service.setThrowExceptionCallback((Exception ex) ->
		{
			ex.printStackTrace();
		});

		
		
		
		
		MConfig mconfig=configGeter.getMConfig();
		HashSet<MRule> rules = null;
		try
		{
			rules=rulesGeter.getRules();
		}catch(FileUnableParseExcption ex)
		{
			ex.printStackTrace();
			System.out.println("无法读取规则文件，请检查文件是否设置正确和是否为JSON格式！");
			System.exit(1);
		}
		catch(InvalidRuleLocalPathException ex)
		{
//			ex.printStackTrace();
			System.out.println(ex.getMessage());
			System.exit(1);
		}

		File clientJar=new File(mconfig.clientJAR);

		if(clientJar.exists())
		{
			if(clientJar.isDirectory())
			{
				System.out.println("客户端核心包Jar文件只能是文件，不能是文件夹");
				System.exit(1);
			}else
			{
				//检查规则中的每个文件夹是否存在
				for(MRule rule : rules)
				{
					if(!new File(rule.getLocalRootDir().getName()).exists())
					{
						System.out.println("文件夹："+rule.getLocalRootDir().getName()+"不存在");
						Runtime.getRuntime().exit(1);
					}
				}
				
				ClientJAR cjar=new ClientJAR(clientJar);
				service.load(mconfig.port, mconfig.maxDownstreamSpeed, mconfig.maxOnlineClient, rules.toArray(new MRule[0]), cjar);

				System.out.println("配置信息：");
				System.out.println(mconfig.toString());

				System.out.println("读取到"+rules.size()+"条同步规则");
				for(MRule rule : rules)
				{
					System.out.println("服务端路径："+rule.getLocalRootDir().getName()+"     客户端路径："+rule.getRemotePathString());
				}

				boolean runS=service.startService();

				if(runS)
				{
					System.out.println("可以使用Enter键来退出了");
					Scanner scanner=new Scanner(System.in);
					scanner.nextLine();
					System.out.println("正在退出");
					service.stopService();
				}else
				{
					System.out.println("启动失败");
					service.exit();
				}

			}
		}else
		{
			System.out.println("找不到客户端核心包Jar文件，（"+mconfig.clientJAR+"），请检查文件是否存在");
			System.exit(1);
		}
	}
	
}