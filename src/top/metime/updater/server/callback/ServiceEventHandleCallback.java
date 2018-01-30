package top.metime.updater.server.callback;

public interface ServiceEventHandleCallback
{
	public void onServiceStarted(int port);
	public void onServiceStopped(int opcode, String reson);
}
