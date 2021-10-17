package hu.szbuli.smarthome.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import hu.szbuli.smarthome.gateway.stat.MessageStats;
import hu.szbuli.smarthome.rpi.mqtt.proxy.Gateway;

public class MqttListener {

  private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);

  private String originalTopic;
  private Gateway gateway;
  private Mqtt5AsyncClient mqttClient;
  private MessageStats messageStats;

  public MqttListener(Mqtt5AsyncClient mqttClient, Gateway gateway, String originalTopic,
      MessageStats messageStats) {
    super();
    this.mqttClient = mqttClient;
    this.originalTopic = originalTopic;
    this.gateway = gateway;
    this.messageStats = messageStats;
  }

  public void subscribe(String topic) {
    logger.debug("subscribing to topic: {}", topic);
    mqttClient.subscribeWith()
        .topicFilter(topic)
        .noLocal(true)
        .callback(publish -> {
          try {
            this.gateway.processIncomingMqttMessage(publish, originalTopic);
          } catch (Exception e) {
            logger.error("mqtt receive error", e);
            this.messageStats.mqttError();
          }
        })
        .send();
  }

}
