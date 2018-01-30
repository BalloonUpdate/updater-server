package top.metime.updater.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import top.metime.updater.server.config.ConfigGeter;
import top.metime.updater.server.config.IgnoreGeter;
import top.metime.updater.server.config.RulesGeter;
import top.metime.updater.server.init.CommandLine;
import top.metime.updater.server.init.TrayUI;
import top.metime.updater.server.memory.MConfig;
import top.metime.updater.server.memory.MRule;
import top.metime.updater.server.net.Service;
import top.metime.updater.server.tools.RulesLoader;

public class Main implements ConfigGeter, RulesGeter, IgnoreGeter
{
	private final String PARAMETER_NOUI = "noui";
	
	private final String propertiesFileName = "serverConfig.properties";
	private final String rulesFileName = "serverRules.json";
	
	
	private Thread serviceThread;
	private Service service;
	
	public static void main(String[] args) throws InterruptedException
	{
		System.out.println(new File("").getAbsolutePath());
		
		
		Main m = new Main();
		m.main2(args);
	}
	
	private void main2(String[] args) throws InterruptedException
	{
		service = new Service();
		serviceThread = new Thread(service);
		serviceThread.start();
		
		//Thread.sleep(1000);
		
		if(args.length>0&&args[0].equalsIgnoreCase(PARAMETER_NOUI))
		{
			CommandLine cl = new CommandLine(service, this, this, this);
			cl.run();
		}else{
			TrayUI ui = new TrayUI(service, this, this, this);
		}
		
	}

	@Override
	public MConfig getMConfig() 
	{
		MConfig mconfig = new MConfig();
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(propertiesFileName);
			mconfig.load(fin);
			fin.close();
		} 
		catch (IOException | IllegalArgumentException | IllegalAccessException ex) 
		{
			ex.printStackTrace();
			service.stopService();
			service.exit();
		} 
		finally 
		{
			try {
				fin.close();
			} catch (IOException ex) {ex.printStackTrace();}
		}
		
		return  mconfig;
	}

	@Override
	public HashSet<MRule> getRules() 
	{
		HashSet<MRule> rules = null;
		try
		{
			File rulesFile = new File(rulesFileName);
			FileInputStream fin = new FileInputStream(rulesFile);

			rules = RulesLoader.loadRules(fin);

			fin.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			service.stopService();
			service.exit();
		}
		
		return rules;
	}
	
}