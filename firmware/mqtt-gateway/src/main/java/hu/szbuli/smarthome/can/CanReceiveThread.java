package hu.szbuli.smarthome.can;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.szbuli.smarthome.rpi.mqtt.proxy.Gateway;
import tel.schich.javacan.CanFrame;
import tel.schich.javacan.RawCanChannel;

public class CanReceiveThread extends SafeThread {

  private static final Logger logger = LoggerFactory.getLogger(CanReceiveThread.class);

  private Gateway gateway;

  public CanReceiveThread(Gateway gateway) {
    this.gateway = gateway;
  }

  @Override
  public void doRun() {
    RawCanChannel channel = null;
    try {
      channel = CanConnectionHelper.getChannel();

      while (true) {
        try {
          CanFrame canFrame = channel.read();

          CanMessage cm = new CanMessage();
          cm.setExtId(canFrame.getId());

          ByteBuffer dataBuffer = ByteBuffer.allocate(canFrame.getDataLength());
          canFrame.getData(dataBuffer);
          cm.setData(dataBuffer.array());
          cm.setRtr(canFrame.isRemoteTransmissionRequest());
          gateway.processIncomingCanMessage(cm);
        } catch (Exception e) {
          logger.error("error happened when processing incoming can message", e);
        }
      }
    } catch (IOException e) {
      logger.error("failed to start can receive thread", e);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (IOException e) {
          logger.error("cannot close socket at can receive thread", e);
        }
      }
    }
  }

  @Override
  public void setThreadName() {
    this.threadName = "CAN_RECEIVE_THREAD";

  }

}
