package hu.szbuli.smarthome.can;

import java.io.IOException;

import tel.schich.javacan.CanChannels;
import tel.schich.javacan.CanSocketOptions;
import tel.schich.javacan.NetworkDevice;
import tel.schich.javacan.RawCanChannel;

public class CanConnectionHelper {

  public static RawCanChannel getChannel() throws IOException {
    RawCanChannel channel = CanChannels.newRawChannel(NetworkDevice.lookup("can0"));
    channel.configureBlocking(true);
    channel.setOption(CanSocketOptions.LOOPBACK, false);
    
    return channel;
  }

}
