package top.metime.updater.server.init;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import top.metime.updater.server.config.ConfigGeter;
import top.metime.updater.server.config.IgnoreGeter;
import top.metime.updater.server.config.RulesGeter;
import top.metime.updater.server.callback.ServiceEventHandleCallback;
import top.metime.updater.server.memory.MClientJAR;
import top.metime.updater.server.memory.MConfig;
import top.metime.updater.server.memory.MRule;
import top.metime.updater.server.net.Service;

public class CommandLine 
{
	private Service service;
	
	private ConfigGeter configGeter;
	private RulesGeter rulesGeter;
	private IgnoreGeter ignoreGeter;
	
	
	public CommandLine(Service service, ConfigGeter configGeter, RulesGeter rulesGeter, IgnoreGeter ignoreGeter)
	{
		this.service = service;
		this.configGeter = configGeter;
		this.rulesGeter = rulesGeter;
		this.ignoreGeter = ignoreGeter;
		
		
		service.setClientConnectedCallback((String host, int port)->
		{
			System.out.println(host+":"+port+"连接上来了！");
		});
		
		service.setClientDisconnectedCallback((String host, int port, Object source)->
		{
			System.out.println(host+":"+port+"断开了连接！");
		});
		
		service.setServiceEventHandleCallback(new ServiceEventHandleCallback()
		{
			@Override
			public void onServiceStarted(int port) 
			{
				System.out.println("服务已启动！");
			}

			@Override
			public void onServiceStopped(int opcode, String reson) 
			{
				System.out.println("服务已停止！");
				service.exit();
			}
			
		});
		
		service.setThrowExceptionListener((Exception ex)->
		{
			ex.printStackTrace();
		});
	}
	
	public void run()
	{
		reload();
		
		boolean runS = service.startService();
		
		if(runS)
		{
			System.out.println("可以使用Enter键来退出了");
			Scanner scanner = new Scanner(System.in);
			scanner.nextLine();
			System.out.println("正在退出");
			service.stopService();
		}else{
			System.out.println("启动失败");
			service.exit();
		}
		
	}
	
	private void reload()
	{
		MConfig mconfig = configGeter.getMConfig();
		HashSet<MRule> rules = rulesGeter.getRules();
		
		if(rules==null)
		{
			System.out.println("无法读取规则文件，请检查文件是否设置正确和是否为JSON格式！");
			System.exit(1);
		}
		
		MClientJAR cjar = new MClientJAR(new File(mconfig.clientJAR));
		service.load(mconfig.port, mconfig.maxDownstreamSpeed, mconfig.maxOnlineClient, rules.toArray(new MRule[0]), cjar);
		
		System.out.println("配置信息：");
		System.out.println(mconfig.toString());
		
		System.out.println("读取到"+rules.size()+"条同步规则");
		for(MRule rule : rules)
		{
			System.out.println("服务端路径："+rule.getLocalRootDir().getName()+"     客户端路径："+rule.getRemotePathString());
		}
		
		
	}
	
}
