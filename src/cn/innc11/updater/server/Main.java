package cn.innc11.updater.server;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;

import cn.innc11.updater.server.net.Service.*;
import cn.innc11.updater.server.excption.FileUnableParseExcption;
import cn.innc11.updater.server.excption.InvalidRuleLocalPathException;
import cn.innc11.updater.server.structure.RuleInstance;
import cn.innc11.updater.server.net.Service;
import cn.innc11.updater.server.tools.RulesLoader;

public class Main
{
	Thread serviceThread;
	Service service;
	byte[] clientJAR;
	
	public static void main(String[] args) throws InterruptedException, IOException
	{
		Main m = new Main();
		m.main2(args);
	}
	
	private void main2(String[] args) throws InterruptedException, IOException
	{
		service = new Service();
		serviceThread = new Thread(service);
		serviceThread.start();//启动线程
		
		initCommandLine(service);
	}

	public Config getConfig()
	{
		Config mconfig = new Config();
		
		try(FileInputStream fileIs = new FileInputStream("serverConfig.properties"))
		{
			mconfig.load(fileIs);
		}
		catch (IOException | IllegalArgumentException | IllegalAccessException ex) 
		{
			ex.printStackTrace();
			service.stopService();
			service.forceExit();
		} 

		return  mconfig;
	}

	public HashSet<RuleInstance> getRules() throws FileUnableParseExcption, InvalidRuleLocalPathException
	{
		String rulesFileName = "serverRules.json";
		HashSet<RuleInstance> rules = null;

		try(FileInputStream fileIs = new FileInputStream(rulesFileName))
		{
			rules = RulesLoader.loadRules(fileIs, "./"+rulesFileName); // 可能因为配置文件什么都没有写而返回空集合
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			service.stopService();
			service.forceExit();
		}
		
		return rules;
	}
	
	
	private void initCommandLine(Service service) throws IOException
	{
		Config mconfig = getConfig();
		HashSet<RuleInstance> rules = null;
		try	{
			rules = getRules();
		}
		catch(FileUnableParseExcption ex)
		{
			ex.printStackTrace();
			System.out.println("无法读取规则文件，请检查文件是否设置正确和是否为JSON格式!");
			System.exit(1);
		}
		catch(InvalidRuleLocalPathException ex)
		{
			System.out.println("错误: "+ex.getMessage());
			System.exit(1);
		}

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("core.jar");

		if(inputStream != null)
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();

			byte[] buf = new byte[1024];
			int count;

			while ((count = inputStream.read(buf)) !=-1)
			{
				bout.write(buf, 0, count);
			}

			inputStream.close();
			clientJAR = bout.toByteArray();

			System.out.println("核心包大小: "+bout.size());

			for(RuleInstance rule : rules) // 检查规则中的每个文件夹是否存在
			{
				if(!new File(rule.localFolder.getName()).exists())
				{
					System.out.println("文件夹； "+rule.localFolder.getName()+"不存在");
					Runtime.getRuntime().exit(1);
				}
			}

			service.load(mconfig.port, mconfig.maxDownstreamSpeed, mconfig.maxOnlineClient, rules.toArray(new RuleInstance[0]), clientJAR);

			System.out.println("配置信息: ");
			System.out.println(mconfig.toString());

			System.out.println("读取到"+rules.size()+"条同步规则");
			for(RuleInstance rule : rules)
			{
				System.out.println("服务端路径: "+rule.localFolder.getName()+"     客户端路径: "+rule.remotePath);
			}

			boolean isRun = service.startService();

			if(isRun)
			{
				System.out.println("退出请按下Enter键");
				Scanner scanner = new Scanner(System.in);
				scanner.nextLine();
				System.out.println("正在退出");
				service.stopService();
			}else{
				System.out.println("启动失败");
				service.forceExit();
			}

		}else{
			System.out.println("找不到客户端核心包Jar文件，请确认文件完整性!");
			System.exit(1);
		}
	}
	
}