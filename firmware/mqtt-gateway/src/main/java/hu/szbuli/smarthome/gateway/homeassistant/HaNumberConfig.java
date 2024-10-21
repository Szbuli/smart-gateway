package hu.szbuli.smarthome.gateway.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HaNumberConfig extends HAEntityConfig {
    @JsonProperty("command_topic")
    private String commandTopic;
    private String mode;
    private boolean retain = true;

    public String getCommandTopic() {
        return commandTopic;
    }

    public void setCommandTopic(String commandTopic) {
        this.commandTopic = commandTopic;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    

    
}
