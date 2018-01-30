package top.metime.updater.server.init;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import javax.swing.JOptionPane;
import top.metime.updater.server.config.ConfigGeter;
import top.metime.updater.server.config.IgnoreGeter;
import top.metime.updater.server.config.RulesGeter;
import top.metime.updater.server.callback.ServiceEventHandleCallback;
import top.metime.updater.server.callback.ThrowExceptionCallback;
import top.metime.updater.server.memory.MClientJAR;
import top.metime.updater.server.memory.MConfig;
import top.metime.updater.server.memory.MRule;
import top.metime.updater.server.net.Service;
import top.metime.updater.server.view.AdvTray;
import top.metime.updater.server.view.ImageData;

public class TrayUI
{
	private static final String START = "Run";
	private static final String STOP = "Stop";
	
	private ConfigGeter configGeter;
	private RulesGeter rulesGeter;
	private IgnoreGeter ignoreGeter;
	
	private AdvTray tray;
	private Service service;
	
	private MenuItem runAndStop = null;
	private MenuItem reload = null;
	private MenuItem quit = null;
		
	public TrayUI(Service service, ConfigGeter configGeter, RulesGeter rulesGeter, IgnoreGeter ignoreGeter)
	{
		this.service = service;
		this.configGeter = configGeter;
		this.rulesGeter = rulesGeter;
		this.ignoreGeter = ignoreGeter;
		
		tray = new AdvTray();
		
		runAndStop = new MenuItem("RunAndStop");
		reload = new MenuItem("Reload");
		quit = new MenuItem("Exit");
		
		//添加菜单项目
		tray.addMenuItem(runAndStop);
		tray.addMenuItem(reload);
		tray.addMenuItem(quit);
		
		initEventHandle();
		
		tray.setImage(ImageData.RED);
		
		updateRunState();
		
		tray.showTray();
		reload();
		
	}
	
	private void updateRunState()
	{
		runAndStop.setLabel(service.isRunned()?STOP:START);
		reload.setEnabled(!service.isRunned());
		quit.setEnabled(!service.isRunned());
	}
	
	
	private void initEventHandle()
	{
		runAndStop.addActionListener((ActionEvent e)->{
			runAndStopListener();
		});
		
		reload.addActionListener((ActionEvent e) -> {
			reloadListener();
		});
		
		quit.addActionListener((ActionEvent e) -> {
			QuitLIstener();
		});
		
		
		service.setClientConnectedCallback((String host, int port) ->
		{
			tray.setTiptool("正在监听的端口："+service.getPort());
			tray.displayMessage("信息", "IP地址："+host+"\n端口："+port+"\n连接上来了！");
		});
		
		service.setClientDisconnectedCallback((String host, int port, Object source)->
		{
			tray.setTiptool("正在监听的端口："+service.getPort());
			tray.displayMessage("信息", "IP地址："+host+"\n端口："+port+"\n断开了连接！");
		});
		
		service.setServiceEventHandleCallback(new ServiceEventHandleCallback()
		{
			@Override
			public void onServiceStarted(int port) 
			{
				tray.setImage(ImageData.GREEN);
				tray.displayMessage("信息", "现在已始开启监听"+port+"！");
				tray.setTiptool("当前连接的客户端：0");
				
				updateRunState();//刷新UI显示
			}

			@Override
			public void onServiceStopped(int opcode, String reson) 
			{
				tray.setImage(ImageData.RED);
				tray.displayMessage("重要", "现在已关闭\n原因："+reson+"！\n"+"关闭代码："+opcode);
				tray.setTiptool("");
				
				updateRunState();//刷新UI显示
			}
		});
			
		service.setThrowExceptionListener((Exception e)->
		{
			tray.displayMessage("异常", e.getMessage()+"\n"+e.toString());
			e.printStackTrace();
		});
	}
	
	
	private void reload()
	{
		reloadListener();

	}
	
	private void runAndStopListener()
	{
		if(service.isRunned())
		{
			service.stopService();
		}else{
			service.startService();
		}
	}
	
	private void reloadListener()
	{
		MConfig mconfig = configGeter.getMConfig();
		HashSet<MRule> rules = rulesGeter.getRules();
		if(rules==null)
		{
			JOptionPane.showMessageDialog(null, "无法读取，文件格式错误", "读取失败", JOptionPane.ERROR_MESSAGE);
		}else{
			System.out.println(mconfig.clientJAR);
			MClientJAR cjar = new MClientJAR(new File(mconfig.clientJAR));
			
			service.load(mconfig.port, mconfig.maxDownstreamSpeed, mconfig.maxOnlineClient, rules.toArray(new MRule[0]), cjar);
			tray.displayMessage("信息", "载入："+rules.size()+"条规则！\n"+mconfig.toString());
		}
		
	}
	
	private void QuitLIstener()
	{
		tray.hideTray();
		service.exit();
	}
}
