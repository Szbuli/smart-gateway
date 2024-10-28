package hu.szbuli.smarthome.gateway.homeassistant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.szbuli.smarthome.can.CanMessage;
import hu.szbuli.smarthome.gateway.state.Availability;
import hu.szbuli.smarthome.gateway.util.NumberUtils;
import hu.szbuli.smarthome.mqtt.MqttManager;
import hu.szbuli.smarthome.mqtt.MqttTopic;
import hu.szbuli.smarthome.rpi.mqtt.proxy.ConversionConfig;

public class DiscoveryManager {

  private static final Logger logger = LoggerFactory.getLogger(DiscoveryManager.class);

  private ObjectMapper objectMapper;
  private MqttManager mqttManager;
  private String gatewayName;
  private Map<Integer, DeviceType> deviceTypeMap;

  public DiscoveryManager(MqttManager mqttManager, DeviceType[] deviceTypes, String gatewayName) {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.mqttManager = mqttManager;
    this.gatewayName = gatewayName;
    this.deviceTypeMap =
        Arrays.stream(deviceTypes).collect(Collectors.toMap(DeviceType::getDeviceTypeId, dt -> dt));
  }

  public void configure(String type, Map<Integer, ConversionConfig> can2Mqtt, CanMessage canMessage, boolean retain)
      throws JsonProcessingException {
    byte[] data = canMessage.getData();
    int deviceId = canMessage.getDeviceId();
    int canStateTopicId = NumberUtils.uint16ToInteger(Arrays.copyOfRange(data, 4, 6));

    MqttTopic haDiscoveryTopic =
        new MqttTopic(can2Mqtt.get(canMessage.getTopicId()).getMqttTopic());
    haDiscoveryTopic.injectValues("deviceId", deviceId);
    haDiscoveryTopic.injectValues("sensorId", canStateTopicId);

    MqttTopic stateTopic = new MqttTopic(can2Mqtt.get(canStateTopicId).getMqttTopic());
    stateTopic.injectValues("deviceId", deviceId);

    int deviceTypeId = NumberUtils.uint8ToInteger(data[3]);
    String version = getVersion(data);

    HADeviceConfig deviceConfig = getDeviceConfig(deviceId, deviceTypeId, version);

    HAEntityConfig entityConfig = createSpecificEntityConfig(type, stateTopic);
    entityConfig.setDevice(deviceConfig);

    if (data.length > 6) {
      int canAvailabilityTopicId = NumberUtils.uint16ToInteger(Arrays.copyOfRange(data, 6, 8));

      MqttTopic availabilityTopic =
          new MqttTopic(can2Mqtt.get(canAvailabilityTopicId).getMqttTopic());
      availabilityTopic.injectValues("deviceId", deviceId);

      entityConfig.setAvailabilityTopic(availabilityTopic.getTopic());
    }

    entityConfig.setUniqueId(stateTopic.getTopic());
    entityConfig.setName(stateTopic.getTopic());

    if (shouldResetDiscoveryTopic(data)) {
      mqttManager.publishMqttMessage(haDiscoveryTopic.getTopic(), "", false);
    } else {
      String configString = objectMapper.writeValueAsString(entityConfig);
      mqttManager.publishMqttMessage(haDiscoveryTopic.getTopic(), configString, retain);
    }

  }

  private boolean shouldResetDiscoveryTopic(byte[] data) {
    return NumberUtils.uint8ToInteger(data[0]) == 0 && NumberUtils.uint8ToInteger(data[0]) == 0
        && NumberUtils.uint8ToInteger(data[0]) == 0;
  }

  private HAEntityConfig createSpecificEntityConfig(String type, MqttTopic stateTopic) {
    switch (type) {
      case "switch":
        HASwitchConfig haSwitchConfig = new HASwitchConfig();
        haSwitchConfig.setCommandTopic(stateTopic.getTopic());
        return haSwitchConfig;
      case "binary_sensor":
        HABinarySensorConfig haBinarySensorConfig = new HABinarySensorConfig();
        haBinarySensorConfig.setStateTopic(stateTopic.getTopic());
        return haBinarySensorConfig;
      case "connectivity_status":
        HABinarySensorConfig haConnecitivityStatusConfig = new HABinarySensorConfig();
        haConnecitivityStatusConfig.setStateTopic(stateTopic.getTopic());
        haConnecitivityStatusConfig.setPayloadOn(Availability.online.toString());
        haConnecitivityStatusConfig.setPayloadOff(Availability.offline.toString());
        haConnecitivityStatusConfig.setDeviceClass("connectivity");
        return haConnecitivityStatusConfig;
      case "sensor":
        HASensorConfig haSensorConfig = new HASensorConfig();
        haSensorConfig.setStateTopic(stateTopic.getTopic());
        return haSensorConfig;
      case "number":
        HaNumberConfig haNumberConfig = new HaNumberConfig();
        haNumberConfig.setCommandTopic(stateTopic.getTopic());
        haNumberConfig.setStateTopic(stateTopic.getTopic());
        haNumberConfig.setMode("box");
        return haNumberConfig;
      default:
        throw new IllegalArgumentException("invalid config type " + type);
    }
  }

  private HADeviceConfig getDeviceConfig(int deviceId, int deviceTypeId, String version) {
    DeviceType deviceType = getDeviceType(deviceTypeId);

    HADeviceConfig deviceConfig = new HADeviceConfig();
    deviceConfig.setSwVersion(version);
    deviceConfig.setModel(deviceType.getModel());
    deviceConfig.setManufacturer(deviceType.getManufacturer());
    deviceConfig.setIdentifiers(Integer.toString(deviceId));
    deviceConfig.setViaDevice(gatewayName);
    deviceConfig.setName(deviceType.getModel() + " - " + deviceId);

    return deviceConfig;
  }

  private String getVersion(byte[] data) {
    return Integer.toString(NumberUtils.uint8ToInteger(data[0])) + "."
        + Integer.toString(NumberUtils.uint8ToInteger(data[1])) + "."
        + Integer.toString(NumberUtils.uint8ToInteger(data[2]));
  }

  private DeviceType getDeviceType(int deviceTypeId) {
    DeviceType deviceType = deviceTypeMap.get(deviceTypeId);
    if (deviceType == null) {
      logger.warn("device type '{}' not found", deviceTypeId);
      return new DeviceType();
    }
    return deviceType;
  }

}
