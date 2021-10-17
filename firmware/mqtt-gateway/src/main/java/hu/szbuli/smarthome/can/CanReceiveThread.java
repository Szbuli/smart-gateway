package hu.szbuli.smarthome.can;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hu.szbuli.smarthome.gateway.stat.MessageStats;
import hu.szbuli.smarthome.rpi.mqtt.proxy.Gateway;

public class CanReceiveThread extends SafeThread {

  private static final Logger logger = LoggerFactory.getLogger(CanReceiveThread.class);

  private Gateway gateway;
  private MessageStats messageStats;

  public CanReceiveThread(Gateway gateway, MessageStats messageStats) {
    this.gateway = gateway;
    this.messageStats = messageStats;
  }

  @Override
  public void doRun() {
    CanConnectionService canService = new CanConnectionServiceImpl();

    while (true) {
      try {
        canService.connectCanSocket();
        while (true) {
          try {
            CanMessage message = canService.receiveCanMessage(-1, 0);
            gateway.processIncomingCanMessage(message);
          } catch (Exception e) {
            messageStats.canError();
            logger.error("can receive error", e);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void setThreadName() {
    this.threadName = "CAN_RECEIVE_THREAD";
  }

}
