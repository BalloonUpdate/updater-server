package top.metime.updater.server.callback;

public interface ClientDisconnectedCallback 
{
	public void onClientDisconnected(String host, int port, Object source);
}
