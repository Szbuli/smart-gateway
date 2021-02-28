package hu.szbuli.smarthome.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import hu.szbuli.smarthome.rpi.mqtt.proxy.Gateway;

public class MqttListener {

  private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);

  private String originalTopic;
  private Gateway gateway;
  private Mqtt5AsyncClient mqttClient;

  public MqttListener(Mqtt5AsyncClient mqttClient, Gateway gateway, String originalTopic) {
    super();
    this.mqttClient = mqttClient;
    this.originalTopic = originalTopic;
    this.gateway = gateway;
  }

  public void subscribe(String topic) {
    logger.info("subscribing to topic: {}", topic);
    mqttClient.subscribeWith()
        .topicFilter(topic)
        .noLocal(true)
        .callback(publish -> {
          try {
            this.gateway.processIncomingMqttMessage(publish, originalTopic);
          } catch (Exception e) {
            logger.error("error happened when processing incoming mqtt message", e);
          }
        })
        .send();
  }

}
