package hu.szbuli.smarthome.rpi.monitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.szbuli.smarthome.rpi.monitor.mqtt.MqttManager;

public class RpiCoreTemperature {
  
  private static final Logger logger = LoggerFactory.getLogger(RpiCoreTemperature.class);

  private MqttManager mqttManager;
  private String rpiCoreTemperatureTopic;

  public RpiCoreTemperature(Properties props, MqttManager mqttManager) {
    this.mqttManager = mqttManager;
    this.rpiCoreTemperatureTopic = props.getProperty("rpiCoreTemperatureTopic");
  }

  public void init() {

  }

  public void readAndPublish() {
    if (rpiCoreTemperatureTopic == null) {
      return;
    }
    String fileName = "/sys/class/thermal/thermal_zone0/temp";

    try (FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader)) {

      String line = bufferedReader.readLine();

      float tempC = (Integer.parseInt(line) / 1000f);
      mqttManager.send(rpiCoreTemperatureTopic, Float.toString(tempC));
    } catch (Exception e) {
      logger.error("error reading core temp", e);
    }
  }

}
