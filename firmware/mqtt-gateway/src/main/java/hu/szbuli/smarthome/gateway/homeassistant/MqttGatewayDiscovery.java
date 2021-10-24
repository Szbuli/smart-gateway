package hu.szbuli.smarthome.gateway.homeassistant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.szbuli.smarthome.gateway.stat.PublishStatsTask;
import hu.szbuli.smarthome.gateway.state.Availability;
import hu.szbuli.smarthome.mqtt.MqttManager;
import hu.szbuli.smarthome.mqtt.MqttTopic;

public class MqttGatewayDiscovery {

  private ObjectMapper objectMapper;
  private MqttManager mqttManager;

  public MqttGatewayDiscovery(MqttManager mqttManager) {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.mqttManager = mqttManager;
  }

  private static final String discoveryTopic =
      "homeassistant/${type}/mqtt-gateway_${deviceId}/${sensorId}/config";
  private static final String model = "Mqtt gateway";
  private static final String manufacturer = "zbl";

  private HABinarySensorConfig getHAStateConfig(HADeviceConfig deviceConfig) {
    String statusTopic = mqttManager.getStatusTopic();

    HABinarySensorConfig entityConfig = new HABinarySensorConfig();
    entityConfig.setDevice(deviceConfig);

    entityConfig.setUniqueId(statusTopic);
    entityConfig.setName(statusTopic);

    entityConfig.setStateTopic(statusTopic);
    entityConfig.setDeviceClass("connectivity");

    entityConfig.setPayloadOn(Availability.online.toString());
    entityConfig.setPayloadOff(Availability.offline.toString());

    return entityConfig;
  }

  private HASensorConfig getStatConfig(HADeviceConfig deviceConfig, String topic) {
    String availabilityTopic = mqttManager.getStatusTopic();
    String statusTopic = mqttManager.getTopicWithBase(topic);

    HASensorConfig entityConfig = new HASensorConfig();
    entityConfig.setDevice(deviceConfig);

    entityConfig.setAvailabilityTopic(availabilityTopic);
    entityConfig.setUniqueId(statusTopic);
    entityConfig.setName(statusTopic);

    entityConfig.setStateTopic(statusTopic);
    entityConfig.setUnitOfMeasurement("/h");

    return entityConfig;
  }

  private void publishConfig(Object entityConfig, String type, String deviceId, String sensorId)
      throws JsonProcessingException {
    String configString = objectMapper.writeValueAsString(entityConfig);

    MqttTopic haDiscoveryTopic = new MqttTopic(discoveryTopic);
    haDiscoveryTopic.injectValues("deviceId", deviceId);
    haDiscoveryTopic.injectValues("sensorId", sensorId);
    haDiscoveryTopic.injectValues("type", type);

    mqttManager.publishMqttMessage(haDiscoveryTopic.getTopic(), configString, true);
  }

  public void publishSelf(String deviceName, String version) throws JsonProcessingException {
    String name = model + " - " + deviceName;

    HADeviceConfig deviceConfig = new HADeviceConfig();
    deviceConfig.setModel(model);
    deviceConfig.setManufacturer(manufacturer);
    deviceConfig.setSwVersion(version);
    deviceConfig.setIdentifiers(name);
    deviceConfig.setName(name);

    publishConfig(getHAStateConfig(deviceConfig), "binary_sensor", deviceName, "status");
    publishConfig(getStatConfig(deviceConfig, PublishStatsTask.CAN_ERROR_TOPIC), "sensor",
        deviceName, "can-error");
    publishConfig(getStatConfig(deviceConfig, PublishStatsTask.MQTT_ERROR_TOPIC), "sensor",
        deviceName, "mqtt-error");
    publishConfig(getStatConfig(deviceConfig, PublishStatsTask.PROCESS_ERROR_TOPIC), "sensor",
        deviceName, "process-error");
    publishConfig(getStatConfig(deviceConfig, PublishStatsTask.PROCESSED_TOPIC), "sensor",
        deviceName, "processed");
  }

}
