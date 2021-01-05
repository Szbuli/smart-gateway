package hu.szbuli.smarthome.gateway.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HAEntityConfig {

  @JsonProperty("unique_id")
  private String uniqueId;
  @JsonProperty("state_topic")
  private String stateTopic;
  @JsonProperty("availability_topic")
  private String availabilityTopic;
  private HADeviceConfig device;
  private String platform;
  private String name;

  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public String getStateTopic() {
    return stateTopic;
  }

  public void setStateTopic(String stateTopic) {
    this.stateTopic = stateTopic;
  }

  public String getAvailabilityTopic() {
    return availabilityTopic;
  }

  public void setAvailabilityTopic(String availabilityTopic) {
    this.availabilityTopic = availabilityTopic;
  }

  public HADeviceConfig getDevice() {
    return device;
  }

  public void setDevice(HADeviceConfig device) {
    this.device = device;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
