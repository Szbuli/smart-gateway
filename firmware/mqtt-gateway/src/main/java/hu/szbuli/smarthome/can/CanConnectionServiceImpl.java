package hu.szbuli.smarthome.can;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.entropia.can.CanSocket;
import de.entropia.can.CanSocket.CanFrame;
import de.entropia.can.CanSocket.CanId;
import de.entropia.can.CanSocket.CanInterface;
import de.entropia.can.CanSocket.Mode;

public class CanConnectionServiceImpl implements CanConnectionService {

  private static final Logger s_logger = LoggerFactory.getLogger(CanConnectionServiceImpl.class);

  private CanSocket socket = null;

  protected void activate() {
    s_logger.info("activating CanConnectionService");
  }

  protected void deactivate() {
    if (this.socket != null) {
      try {
        this.socket.close();
      } catch (IOException e) {
        s_logger.error("Error closing CAN socket");
      }
    }
  }

  @Override
  public void connectCanSocket() throws IOException {
    this.socket = new CanSocket(Mode.RAW);
    this.socket.setLoopbackMode(false);
    this.socket.bind(CanSocket.CAN_ALL_INTERFACES);
  }

  @Override
  public void disconnectCanSocket() throws IOException {
    if (this.socket != null) {
      this.socket.close();
    }
  }

  @Override
  public void sendCanMessage(String ifName, int canId, boolean isRTR, byte[] message) throws IOException {
    if (message.length > 8) {
      // throw new KuraException(KuraErrorCode.OPERATION_NOT_SUPPORTED,
      // "CAN send : Incorrect frame length");
    }

    try {
      CanInterface canif = new CanInterface(this.socket, ifName);
      socket.bind(canif);
      CanId canIdObject = new CanId(canId);
      System.out.println("canId: " + canId);
      canIdObject.setEFFSFF();
      if (isRTR) {
        canIdObject.setRTR();
      }
      socket.send(new CanFrame(canif, canIdObject, message));
    } catch (IOException e) {
      s_logger.error("Error on CanSocket in sendCanMessage: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public CanMessage receiveCanMessage(int canId, int canMask) throws IOException {
    try {
      if (canId >= 0) {
        // this.socket.setCanFilter(canId, canMask);
      }
      CanFrame cf = this.socket.recv();
      CanId ci = cf.getCanId();

      CanMessage cm = new CanMessage();
      cm.setExtId(ci.getCanId_EFF());
      cm.setData(cf.getData());
      cm.setRtr(ci.isSetRTR());
      return cm;
    } catch (IOException e) {
      s_logger.error("Error on CanSocket in receiveCanMessage: {}", e.getMessage());
      throw e;
    }
  }

}
