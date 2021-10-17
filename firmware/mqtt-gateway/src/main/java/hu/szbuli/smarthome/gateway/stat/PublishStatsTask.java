package hu.szbuli.smarthome.gateway.stat;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import hu.szbuli.smarthome.gateway.util.ExponentialSmoothing;
import hu.szbuli.smarthome.mqtt.MqttManager;

public class PublishStatsTask extends TimerTask {

  public static final long DELAY_INTERVAL_MS = TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES);
  private static final long INTERVAL_FACTOR =
      TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS) / DELAY_INTERVAL_MS;
  private static final double TINY = 0.2;

  private MessageStats messageStats;
  private MqttManager mqttManager;

  private double canErrorPerHour = 0;
  private double mqttErrorPerHour = 0;
  private double processedPerHour = 0;
  private double processErrorPerHour = 0;

  public static final String CAN_ERROR_TOPIC = "stats/can-error";
  public static final String MQTT_ERROR_TOPIC = "stats/mqtt-error";
  public static final String PROCESSED_TOPIC = "stats/processed";
  public static final String PROCESS_ERROR_TOPIC = "stats/process-error";

  public PublishStatsTask(MessageStats messageStats, MqttManager mqttManager) {
    super();
    this.messageStats = messageStats;
    this.mqttManager = mqttManager;
  }

  @Override
  public void run() {
    canErrorPerHour = ExponentialSmoothing.smooth(messageStats.getCanError() * INTERVAL_FACTOR,
        canErrorPerHour, TINY);
    mqttErrorPerHour = ExponentialSmoothing.smooth(messageStats.getMqttError() * INTERVAL_FACTOR,
        mqttErrorPerHour, TINY);
    processedPerHour = ExponentialSmoothing.smooth(messageStats.getProcessed() * INTERVAL_FACTOR,
        processedPerHour, TINY);
    processErrorPerHour = ExponentialSmoothing
        .smooth(messageStats.getProcessError() * INTERVAL_FACTOR, processErrorPerHour, TINY);

    messageStats.reset();

    mqttManager.publishMqttMessage(mqttManager.getTopicWithBase(CAN_ERROR_TOPIC),
        String.format("%.2f", canErrorPerHour));
    mqttManager.publishMqttMessage(mqttManager.getTopicWithBase(MQTT_ERROR_TOPIC),
        String.format("%.2f", mqttErrorPerHour));
    mqttManager.publishMqttMessage(mqttManager.getTopicWithBase(PROCESSED_TOPIC),
        String.format("%.2f", processedPerHour));
    mqttManager.publishMqttMessage(mqttManager.getTopicWithBase(PROCESS_ERROR_TOPIC),
        String.format("%.2f", processErrorPerHour));
  }

}
