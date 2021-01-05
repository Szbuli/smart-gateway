package hu.szbuli.smarthome.rpi.mqtt.proxy;

public class MqttConfiguration {
  private String host;
  private int port;
  private String username;
  private String password;
  private String statusTopic;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getStatusTopic() {
    return statusTopic;
  }

  public void setStatusTopic(String statusTopic) {
    this.statusTopic = statusTopic;
  }

}