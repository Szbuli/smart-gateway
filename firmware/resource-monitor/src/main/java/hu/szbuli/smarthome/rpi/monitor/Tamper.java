package hu.szbuli.smarthome.rpi.monitor;

import java.util.Properties;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import hu.szbuli.smarthome.rpi.monitor.mqtt.MqttManager;

public class Tamper implements GpioPinListenerDigital {

  private static final String TAMPER_ON = "ON";
  private static final String TAMPER_OFF = "OFF";

  private MqttManager mqttManager;
  private String topic;

  public Tamper(Properties props, MqttManager mqttManager) {
    this.mqttManager = mqttManager;
    this.topic = props.getProperty("tamperTopic");
  }

  public void init() {
    if (topic == null) {
      return;
    }

    GpioController gpio = GpioFactory.getInstance();
    GpioPinDigitalInput tamperInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00,
        "tamper",
        PinPullResistance.OFF);
    tamperInput.addListener(this);
    if (tamperInput.isHigh()) {
      mqttManager.send(topic, TAMPER_OFF);
    } else {
      mqttManager.send(topic, TAMPER_ON);
    }
  }

  @Override
  public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
    if (event.getEdge().equals(PinEdge.FALLING)) {
      mqttManager.send(topic, TAMPER_ON);
    } else if (event.getEdge() == PinEdge.RISING) {
      mqttManager.send(topic, TAMPER_OFF);
    }

  }

}
