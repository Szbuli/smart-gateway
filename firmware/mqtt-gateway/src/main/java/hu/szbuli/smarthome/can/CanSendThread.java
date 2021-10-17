package hu.szbuli.smarthome.can;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hu.szbuli.smarthome.gateway.stat.MessageStats;

public class CanSendThread extends SafeThread {

  private static final Logger logger = LoggerFactory.getLogger(CanSendThread.class);

  private static int maxQueueSize = 100;
  private MessageStats messageStats;

  public static ArrayBlockingQueue<CanMessage> sendCanQueue =
      new ArrayBlockingQueue<>(maxQueueSize);

  public CanSendThread(MessageStats messageStats) {
    this.messageStats = messageStats;
  }

  @Override
  public void doRun() {
    CanMessage message;

    CanConnectionService canService = new CanConnectionServiceImpl();
    try {
      canService.connectCanSocket();

      while (true) {
        try {
          message = CanSendThread.sendCanQueue.take();
          canService.sendCanMessage("can0", message.getExtId(), false, message.getData());
        } catch (Exception e) {
          this.messageStats.canError();
          logger.error("failed to send can message", e);
        }
      }
    } catch (IOException e) {
      logger.error("failed to connect to can socket", e);
    } finally {
      if (canService != null) {
        try {
          canService.disconnectCanSocket();
        } catch (IOException e) {
          logger.error("failed to disconnect can socket", e);
        }
      }
    }
  }

  @Override
  public void setThreadName() {
    this.threadName = "CAN_SEND_THREAD";
  }

}
