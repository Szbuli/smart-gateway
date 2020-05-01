package hu.szbuli.smarthome.can;

public abstract class SafeThread implements Runnable {
	protected String threadName;
	private volatile Thread t;

	@Override
	public void run() {
		doRun();
		System.out.println("stopped");
	}

	public abstract void doRun();

	public abstract void setThreadName();

	public void start() {
		this.setThreadName();
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

	public void stop() {
		if (t != null) {
			t.interrupt();
			t = null;
		}
	}
}
