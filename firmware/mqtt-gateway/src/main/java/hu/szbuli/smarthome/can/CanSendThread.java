package hu.szbuli.smarthome.can;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class CanSendThread extends SafeThread {

  private static int maxQueueSize = 100;

  public static ArrayBlockingQueue<CanMessage> sendCanQueue = new ArrayBlockingQueue<>(maxQueueSize);

  @Override
  public void doRun() {
    CanMessage message;

    CanConnectionService canService = new CanConnectionServiceImpl();
    try {
      canService.connectCanSocket();

      while (true) {
        message = CanSendThread.sendCanQueue.take();
        canService.sendCanMessage("can0", message.getExtId(), false, message.getData());
      }
    } catch (IOException e2) {
      e2.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (canService != null) {
        try {
          canService.disconnectCanSocket();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void setThreadName() {
    this.threadName = "CAN_SEND_THREAD";
  }

}
