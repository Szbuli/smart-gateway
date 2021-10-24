package hu.szbuli.smarthome.gateway.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HASwitchConfig extends HAEntityConfig {

  @JsonProperty("command_topic")
  private String commandTopic;

  public String getCommandTopic() {
    return commandTopic;
  }

  public void setCommandTopic(String commandTopic) {
    this.commandTopic = commandTopic;
  }

}
