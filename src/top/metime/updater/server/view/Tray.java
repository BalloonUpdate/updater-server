package top.metime.updater.server.view;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tray implements ActionListener 
{
	private BufferedImage launched;
	private BufferedImage unlaunch;
	
	private SystemTray sysTray = SystemTray.getSystemTray();
	private TrayIcon trayIcon;
	private PopupMenu menu = new PopupMenu();
	
	private MenuItem run = new MenuItem("启动");
	private MenuItem stop = new MenuItem("停止");
	private MenuItem reload = new MenuItem("重載");
	private MenuItem quit = new MenuItem("退出");
	
	public Tray() 
	{
		try 
		{
			launched = ImageIO.read(new File("D:/fssLib/start.png"));
			unlaunch = ImageIO.read(new File("D:/fssLib/stop.png"));
		} 
		catch (IOException e) {e.printStackTrace();}
		
		trayIcon = new TrayIcon(unlaunch);
		
		trayIcon.setPopupMenu(menu);
		
		init();
	}
	
	private boolean contains(TrayIcon trayicon)
	{
		TrayIcon[] tis = sysTray.getTrayIcons();
		
		for(TrayIcon per : tis)
		{
			if(per == trayicon)
			{
				return true;
			}
		}
		return false;
	}
	
	public void init()
	{
		menu.add(run);
		menu.add(stop);
  		menu.add(reload);
		menu.add(quit);
		run.addActionListener(this);
		stop.addActionListener(this);
		reload.addActionListener(this);
		quit.addActionListener(this);
	}
	
	private ActionListener RunListener;
	private ActionListener StopListener;
	private ActionListener ReloadListener;
	private ActionListener QuitListener;
	
	
	public void setRunListener(ActionListener l)
	{
		RunListener = l;
	}
	
	public void setStopListener(ActionListener l)
	{
		StopListener = l;
	}
	
	public void setReloadListener(ActionListener l)
	{
		ReloadListener = l;
	}
	
	public void setQuitListener(ActionListener l)
	{
		QuitListener = l;
	}
	
	
	
	public boolean isSupported()
	{
		return SystemTray.isSupported();
	}
	
	public void addTray()
	{
		if(!contains(trayIcon))
		{
			try
			{
				sysTray.add(trayIcon);
			}
			catch (AWTException e) {e.printStackTrace();}
		}
	}
	
	public void removeTray()
	{
		if(contains(trayIcon))
		{
			sysTray.remove(trayIcon);
		}
	}
	
	public void displayMessage(String head, String body, TrayIcon.MessageType type)
	{
		trayIcon.displayMessage(head, body, type);
	}
	
	public void setRunned(String bubble, String tooltip)
	{
		trayIcon.setImage(launched);
		trayIcon.displayMessage("", bubble, TrayIcon.MessageType.INFO);
		trayIcon.setToolTip(tooltip);
	}
	
	public void setStopped(String bubble, String tooltip)
	{
		trayIcon.setImage(unlaunch);
		trayIcon.displayMessage("", bubble, TrayIcon.MessageType.INFO);
		trayIcon.setToolTip(tooltip);
	}
	
	

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		MenuItem s = (MenuItem) e.getSource();
		if(s==run)
		{
			RunListener.actionPerformed(e);
		}
		if(s==stop)
		{
			StopListener.actionPerformed(e);
		}
		if(s==reload)
		{
			ReloadListener.actionPerformed(e);
		}
		if(s==quit)
		{
			
			QuitListener.actionPerformed(e);
		}	
	}
}
