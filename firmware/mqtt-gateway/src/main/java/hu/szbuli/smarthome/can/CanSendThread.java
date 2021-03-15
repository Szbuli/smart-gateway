package hu.szbuli.smarthome.can;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tel.schich.javacan.CanFrame;
import tel.schich.javacan.RawCanChannel;

public class CanSendThread extends SafeThread {

  private static final Logger logger = LoggerFactory.getLogger(CanSendThread.class);

  private static int maxQueueSize = 100;

  public static ArrayBlockingQueue<CanMessage> sendCanQueue = new ArrayBlockingQueue<>(maxQueueSize);

  @Override
  public void doRun() {
    CanMessage message;

    RawCanChannel channel = null;
    try {
      channel = CanConnectionHelper.getChannel();

      while (true) {
        message = CanSendThread.sendCanQueue.take();
        CanFrame frame = CanFrame.createExtended(message.getExtId(), CanFrame.FD_NO_FLAGS, message.getData());

        channel.write(frame);
      }
    } catch (IOException | InterruptedException e) {
      logger.error("failed to start can send thread", e);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (IOException e) {
          logger.error("cannot close socket at can send thread", e);
        }
      }
    }
  }

  @Override
  public void setThreadName() {
    this.threadName = "CAN_SEND_THREAD";
  }

}
