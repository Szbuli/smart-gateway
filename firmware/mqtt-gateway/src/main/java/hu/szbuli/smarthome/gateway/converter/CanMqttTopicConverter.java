package hu.szbuli.smarthome.gateway.converter;

import org.apache.commons.lang3.StringUtils;

public class CanMqttTopicConverter {

  private static final String deviceIdParamString = "${deviceId}";

  public Integer getDeviceId(String configMqttTopic, String realMqttTopic) {
    int index = configMqttTopic.indexOf(deviceIdParamString);
    if (index == -1) {
      return null;
    }
    return Integer.parseInt(StringUtils.substringBetween(realMqttTopic, configMqttTopic.substring(0, index),
        configMqttTopic.substring(index + deviceIdParamString.length(), configMqttTopic.length())));
  }
}
