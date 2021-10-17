package hu.szbuli.smarthome.gateway.stat;

import java.util.Timer;
import hu.szbuli.smarthome.mqtt.MqttManager;

public class MessageStats {

  private int canError;
  private int mqttError;
  private int processed;
  private int processError;

  private MqttManager mqttManager;

  public MessageStats(MqttManager mqttManager) {
    this.mqttManager = mqttManager;
    initResetTask();
  }

  private void initResetTask() {
    Timer timer = new Timer();

    timer.schedule(new PublishStatsTask(this, mqttManager), PublishStatsTask.DELAY_INTERVAL_MS,
        PublishStatsTask.DELAY_INTERVAL_MS);
  }

  public void canError() {
    canError++;
  }

  public void mqttError() {
    mqttError++;
  }

  public void messageProcessed() {
    processed++;
  }

  public void messageProcessError() {
    processError++;
  }

  public void reset() {
    canError = 0;
    mqttError = 0;
    processed = 0;
    processError = 0;
  }

  public int getProcessed() {
    return processed;
  }

  public int getCanError() {
    return canError;
  }

  public int getMqttError() {
    return mqttError;
  }

  public int getProcessError() {
    return processError;
  }

}
