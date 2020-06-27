package hu.szbuli.smarthome.can;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SafeThread implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(SafeThread.class);

  protected String threadName;
  private volatile Thread t;

  @Override
  public void run() {
    doRun();
    logger.info("Stopped {}", threadName);
    System.out.println("stopped");
  }

  public abstract void doRun();

  public abstract void setThreadName();

  public void start() {
    this.setThreadName();
    System.out.println("Starting " + threadName);
    logger.info("Starting {}", threadName);
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
