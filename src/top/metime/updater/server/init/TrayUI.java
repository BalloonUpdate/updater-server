package top.metime.updater.server.init;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import javax.swing.JOptionPane;
import top.metime.updater.server.callback.ServiceEventHandleCallback;
import top.metime.updater.server.config.ConfigGeter;
import top.metime.updater.server.config.RulesGeter;
import top.metime.updater.server.excption.FileUnableParseExcption;
import top.metime.updater.server.excption.InvalidRuleLocalPathException;
import top.metime.updater.server.memory.ClientJAR;
import top.metime.updater.server.memory.MConfig;
import top.metime.updater.server.memory.MRule;
import top.metime.updater.server.net.Service;
import top.metime.updater.server.view.AdvTray;
import top.metime.updater.server.view.ImageData;

/**
 * 该类表示以托盘UI形式来初始化
 *
 * @author innc
 */
public class TrayUI
{
	private static final String START = "Run";
	private static final String STOP = "Stop";
	
	private ConfigGeter configGeter;
	private RulesGeter rulesGeter;
	
	private AdvTray tray;
	private Service service;
	
	private MenuItem runAndStop = null;
	private MenuItem reload = null;
	private MenuItem quit = null;
		
	/**
	 *
	 * @param service
	 * @param configGeter
	 * @param rulesGeter
	 */
	public TrayUI(Service service, ConfigGeter configGeter, RulesGeter rulesGeter)
	{
		this.service = service;
		this.configGeter = configGeter;
		this.rulesGeter = rulesGeter;
		
		tray = new AdvTray();
		
		runAndStop = new MenuItem("Run/Stop");
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
			runOrStopListener();
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
			
		service.setThrowExceptionCallback((Exception e)->
		{
			tray.displayMessage("异常", e.getMessage()+"\n"+e.toString());
			e.printStackTrace();
		});
	}
	
	
	private void reload()
	{
		reloadListener();

	}
	
	private void runOrStopListener()
	{
		if(service.isRunned())
		{
			service.stopService();
		}else{
			if(!service.startService())
			{
				tray.displayMessage("规则为空", "规则为空或者规则文件加载失败");
			}
		}
	}
	
	private void reloadListener()
	{
		MConfig mconfig = configGeter.getMConfig();
		HashSet<MRule> rules = null;
		try
		{
			rules=rulesGeter.getRules();
		}catch(FileUnableParseExcption ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "失败", JOptionPane.ERROR_MESSAGE);
		}
		catch(InvalidRuleLocalPathException ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "规则无效", JOptionPane.ERROR_MESSAGE);
//			System.exit(1);
		}
		
		if(rules==null)
		{
//			JOptionPane.showMessageDialog(null, "无法读取，文件格式错误", "读取失败", JOptionPane.ERROR_MESSAGE);
		}else{
			System.out.println(mconfig.clientJAR);
			
			File clientJar = new File(mconfig.clientJAR);
			
			if(clientJar.exists())
			{
				if(clientJar.isDirectory())
				{
					tray.displayMessage("无法使用客户端核心包文件", "客户端核心包Jar文件只能是文件，不能是文件夹");
				}else{
					
					/*
					HashSet<MRule> noexists = new HashSet<>();
					for(MRule rule : rules)
					{
						if(rule.getLocalRootDir().)
					}
*/
					boolean f = true;
					//检查规则中的每个文件夹是否存在
					for(MRule rule : rules)
					{
						if(!new File(rule.getLocalRootDir().getName()).exists())
						{
							f = false;
							tray.displayMessage("找不到文件夹", "文件夹："+rule.getLocalRootDir().getName()+"不存在");
						}else{
							tray.displayMessage("找到", new File(rule.getLocalRootDir().getName()).getAbsolutePath());
						}
					}

					if(f)
					{                                                       
						ClientJAR cjar = new ClientJAR(clientJar);
						
						service.load(mconfig.port, mconfig.maxDownstreamSpeed, mconfig.maxOnlineClient, rules.toArray(new MRule[0]), cjar);
						tray.displayMessage("信息", "载入："+rules.size()+"条规则！\n"+mconfig.toString());
					}
					
				}
			}else{
				tray.displayMessage("找不到核心包文件", "找不到客户端核心包Jar文件，（"+mconfig.clientJAR+"），请检查文件是否存在");
			}
			
			
		}
		
	}
	
	private void QuitLIstener()
	{
		tray.hideTray();
		service.exit();
	}
}
