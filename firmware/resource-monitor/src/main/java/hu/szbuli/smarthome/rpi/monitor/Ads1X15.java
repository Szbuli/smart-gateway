package hu.szbuli.smarthome.rpi.monitor;

import java.io.IOException;
import java.util.Properties;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import hu.szbuli.smarthome.rpi.monitor.mqtt.MqttManager;

public class Ads1X15 {

  private MqttManager mqttManager;
  private String topic;

  private I2CDevice device;

  public Ads1X15(Properties props, MqttManager mqttManager) {
    this.mqttManager = mqttManager;
    this.topic = props.getProperty("voltageTopic");
  }

  public void init() throws IOException, UnsupportedBusNumberException, InterruptedException {
    if (topic == null) {
      return;
    }
    
    // Create I2C bus
    I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
    // Get I2C device, ADS1115 I2C address is 0x48(72)
    device = bus.getDevice(0x48);

    byte[] config = { (byte) 0x84, (byte) 0x83 };
    // Select configuration register
    // AINP = AIN0 and AINN = AIN1, +/- 2.048V, Continuous conversion mode, 128 SPS
    device.write(0x01, config, 0, 2);

    Thread.sleep(500);
  }

  public void getValue() throws IOException {
    if (topic == null) {
      return;
    }
    
    // Read 2 bytes of data
    // raw_adc msb, raw_adc lsb
    byte[] data = new byte[2];
    device.read(0x00, data, 0, 2);

    // Convert the data
    int raw_adc = ((data[0] & 0xFF) * 256) + (data[1] & 0xFF);
    if (raw_adc > 32767) {
      raw_adc -= 65535;
    }

    mqttManager.send(topic, Integer.toString(raw_adc));
  }

}
