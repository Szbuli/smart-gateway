package hu.szbuli.smarthome.mqtt;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import hu.szbuli.smarthome.rpi.mqtt.proxy.Gateway;

public class MqttListener {

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
    mqttClient.subscribeWith()
        .topicFilter(topic)
        .noLocal(true)
        .callback(publish -> {
          this.gateway.processIncomingMqttMessage(publish, originalTopic);
        })
        .send();
  }

}
