package hu.szbuli.smarthome.mqtt;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;

import hu.szbuli.smarthome.gateway.heartbeat.HeartBeatService;
import hu.szbuli.smarthome.gateway.stat.MessageStats;
import hu.szbuli.smarthome.rpi.mqtt.proxy.MqttConfiguration;

public class MqttManager {

  private static final Logger logger = LoggerFactory.getLogger(MqttManager.class);

  private Mqtt5AsyncClient mqttClient;
  private String baseTopic;
  private MqttConfiguration mqttConfiguration;
  private MessageStats messageStats;
  private String deviceName;
  private String statusTopic;

  private static final String DEFAULT_STATUS_TOPIC = "status";

  public MqttManager(MqttConfiguration mqttConfiguration, String baseTopic, String deviceName) {
    this.mqttConfiguration = mqttConfiguration;
    this.baseTopic = baseTopic;
    this.deviceName = deviceName;
    this.statusTopic = getTopicWithBase(DEFAULT_STATUS_TOPIC);
  }

  public MqttManager(MqttConfiguration mqttConfiguration, String statusTopic) {
    this.mqttConfiguration = mqttConfiguration;
    this.statusTopic = statusTopic;
  }

  public void connect(boolean reconnect, boolean logEvents) {
    Mqtt5ClientBuilder clientBuilder = MqttClient.builder()
        .addConnectedListener(context -> {
          publishMqttMessage(statusTopic, HeartBeatService.ONLINE_PAYLOAD, true);
        })
        .useMqttVersion5()
        .willPublish()
        .topic(statusTopic)
        .retain(true)
        .payload(HeartBeatService.OFFLINE_PAYLOAD)
        .applyWillPublish()
        .simpleAuth()
        .username(mqttConfiguration.getUsername())
        .password(mqttConfiguration.getPassword()
            .getBytes())
        .applySimpleAuth()
        .identifier(UUID.randomUUID()
            .toString())
        .serverHost(mqttConfiguration.getHost())
        .serverPort(mqttConfiguration.getPort());
    // .sslWithDefaultConfig()

    if (reconnect) {
      clientBuilder.automaticReconnectWithDefaultConfig();
    }

    if (logEvents) {
      clientBuilder.addDisconnectedListener(context -> {
        logger.error("disconected from mqtt ({})", Instant.now(), context.getCause());
      })
          .addConnectedListener(context -> {
            logger.info("connected to mqtt ({})", Instant.now());
          });
    }

    mqttClient = clientBuilder.buildAsync();

    mqttClient.connectWith()
        .send();
  }

  public void publishMqttMessage(String topic, byte[] payload) {
    this.publishMqttMessage(topic, payload, false);
  }

  public void publishMqttMessage(String topic, String payload) {
    this.publishMqttMessage(topic, payload.getBytes(), false);
  }

  public void publishMqttMessage(String topic, String payload, boolean retain) {
    this.publishMqttMessage(topic, payload.getBytes(), retain);
  }

  public void publishMqttMessage(String topic, byte[] payload, boolean retain) {
    try {
      mqttClient.publishWith()
          .topic(topic)
          .payload(payload)
          .qos(MqttQos.AT_MOST_ONCE)
          .retain(retain)
          .send();
    } catch (Exception e) {
      logger.error("publish mqtt message failed", e);
      if (messageStats != null) {
        messageStats.mqttError();
      }
    }
  }

  public Mqtt5AsyncClient getMqttClient() {
    return mqttClient;
  }

  public void disconnect() {
    if (mqttClient != null) {
      publishMqttMessage(statusTopic, HeartBeatService.OFFLINE_PAYLOAD, true);
      mqttClient.disconnect();
    }
  }

  public String getTopicWithBase(String topic) {
    return baseTopic + "/" + deviceName + "/mqtt-gateway/" + topic;
  }

  public void setMessageStats(MessageStats messageStats) {
    this.messageStats = messageStats;
  }

  public String getStatusTopic() {
    return statusTopic;
  }

}
