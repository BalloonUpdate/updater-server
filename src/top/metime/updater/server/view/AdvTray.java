package top.metime.updater.server.view;

import static top.metime.updater.server.view.ImageData.*;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

public class AdvTray 
{
	private SystemTray sys = SystemTray.getSystemTray();
	private TrayIcon tray;
	
	private PopupMenu menu = new PopupMenu();
	
	public AdvTray() //构造
	{
		tray = new TrayIcon(getImage(INIT));
		tray.setPopupMenu(menu);
	}
	
	
	public void setImage(BufferedImage image)//设置图标
	{
		tray.setImage(image);
	}
	public void setImage(byte[] image)//设置图标
	{
		tray.setImage(getImage(image));
	}
	
	
	public void addMouseListener(MouseListener ml)//添加鼠标监听器
	{
		tray.addMouseListener(ml);
	}
	public void removeMouseListener(MouseListener ml)//移除鼠标监听器
	{
		tray.removeMouseListener(ml);
	}
	
	
	public void addMenuItem(MenuItem mi)//添加菜单条目
	{
		menu.add(mi);
	}
	public void removeMenuItem(MenuItem mi)//移除菜单条目
	{
		menu.remove(mi);
	}
	
	
	public void displayMessage(String summary, String content)//弹出提示消息
	{
		tray.displayMessage(summary, content, TrayIcon.MessageType.NONE);
	}
	
	
	public void setTiptool(String message)
	{
		tray.setToolTip(message);
	}
	
	
	public void showTray()//显示
	{
		if(!contains(tray))
		{
			try
			{
				sys.add(tray);
			}
			catch (AWTException e) {e.printStackTrace();}
		}
	}
	public void hideTray()//隐藏
	{
		if(contains(tray))
		{
			sys.remove(tray);
		}
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private boolean contains(TrayIcon trayicon)
	{
		TrayIcon[] tis = sys.getTrayIcons();

		for(TrayIcon per : tis)
		{
			if(per == trayicon)
			{
				return true;
			}
		}
		return false;
	}
	
	
	//---------------------------------------------Tool Mothed
	private BufferedImage getImage(byte[] bytes)
	{
		BufferedImage image = null;
		try
		{                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
			ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			ImageReader reader = ImageIO.getImageReadersBySuffix("png").next();
			reader.setInput(ImageIO.createImageInputStream(bin));
			image = reader.read(0);
		}
		catch (IOException e) {e.printStackTrace();}
		return image;
	}
	
	@SuppressWarnings("unused")
	private byte[] getImageBytes(File file, String suffix) throws IOException
	{
		//suffix = png|jpg
		ImageReader bi = ImageIO.getImageReadersBySuffix(suffix).next();
		bi.setInput(ImageIO.createImageInputStream(file));
		
		BufferedImage i = bi.read(0);
		ImageWriter iw = ImageIO.getImageWritersBySuffix(suffix).next();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		iw.setOutput(ImageIO.createImageOutputStream(bout));
		iw.write(i);
		
		return bout.toByteArray();
	}
}
