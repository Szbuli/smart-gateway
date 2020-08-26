package hu.szbuli.smarthome.can;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.szbuli.smarthome.rpi.mqtt.proxy.Gateway;

public class CanReceiveThread extends SafeThread {

  private static final Logger logger = LoggerFactory.getLogger(CanReceiveThread.class);

  private Gateway gateway;

  public CanReceiveThread(Gateway gateway) {
    this.gateway = gateway;
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
            logger.error("error happened when processing incoming can message", e);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void setThreadName() {
    this.threadName = "CAN_RECIEVE_THREAD";

  }

}
