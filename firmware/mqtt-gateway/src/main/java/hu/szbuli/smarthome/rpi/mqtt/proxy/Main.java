package hu.szbuli.smarthome.rpi.mqtt.proxy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import hu.szbuli.smarthome.can.CanReceiveThread;
import hu.szbuli.smarthome.can.CanSendThread;
import hu.szbuli.smarthome.gateway.heartbeat.HeartBeatService;
import hu.szbuli.smarthome.gateway.homeassistant.DeviceType;
import hu.szbuli.smarthome.gateway.homeassistant.DiscoveryManager;
import hu.szbuli.smarthome.mqtt.MqttManager;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static Mqtt5AsyncClient client = null;

  public static void main(String[] args) throws IOException, ParseException {
    Options options = new Options();
    options.addOption("m", "mqtt", true, "mqtt config file");
    options.addOption("g", "gateway", true, "gateway config file");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    final String mqttConfigFile = cmd.getOptionValue("m", "./mqtt.properties");
    final String gatewayConfigFile = cmd.getOptionValue("g", "./config.csv");
    final String deviceTypesConfigFile = cmd.getOptionValue("d", "./deviceTypes.json");

    Properties prop = new Properties();
    prop.load(new FileInputStream(mqttConfigFile));

    MqttConfiguration mqttConfiguration = new MqttConfiguration();
    mqttConfiguration.setStatusTopic(prop.getProperty("statusTopic"));
    mqttConfiguration.setHost(prop.getProperty("host"));
    mqttConfiguration.setPort(Integer.parseInt(prop.getProperty("port")));
    mqttConfiguration.setUsername(prop.getProperty("username"));
    mqttConfiguration.setPassword(prop.getProperty("password"));

    Mqtt5AsyncClient mqttClient = initMqttClient(mqttConfiguration);

    MqttManager mqttManager = new MqttManager(mqttClient);

    DeviceType[] deviceTypes = parseDeviceTypes(deviceTypesConfigFile);
    DiscoveryManager discoveryManager = new DiscoveryManager(mqttManager, deviceTypes, prop.getProperty("gatewayName"));
    Gateway gateway = new Gateway(gatewayConfigFile, mqttManager, discoveryManager, CanSendThread.sendCanQueue);

    CanSendThread canSendThread = new CanSendThread();
    canSendThread.start();

    CanReceiveThread canRecieveThread = new CanReceiveThread(gateway);
    canRecieveThread.start();
  }

  private static DeviceType[] parseDeviceTypes(String deviceTypesConfigFile) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(Paths.get(deviceTypesConfigFile).toFile(), DeviceType[].class);
  }

  private static Mqtt5AsyncClient initMqttClient(MqttConfiguration mqttConfiguration) throws FileNotFoundException, IOException {

    client = MqttClient.builder()
        .automaticReconnectWithDefaultConfig()
        .addDisconnectedListener(context -> {
          logger.error("disconected from mqtt ({})", Instant.now(), context.getCause());
        })
        .addConnectedListener(context -> {
          logger.info("connected to mqtt ({})", Instant.now());
          client.publishWith()
              .topic(mqttConfiguration.getStatusTopic())
              .payload(HeartBeatService.ONLINE_PAYLOAD)
              .qos(MqttQos.AT_MOST_ONCE)
              .retain(true)
              .send();
        })
        .useMqttVersion5()
        .willPublish()
        .topic(mqttConfiguration.getStatusTopic()).retain(true)
        .payload(HeartBeatService.OFFLINE_PAYLOAD)
        .applyWillPublish()
        .simpleAuth()
        .username(mqttConfiguration.getUsername())
        .password(mqttConfiguration.getPassword().getBytes())
        .applySimpleAuth()
        .identifier(UUID.randomUUID().toString())
        .serverHost(mqttConfiguration.getHost())
        .serverPort(mqttConfiguration.getPort())
        // .sslWithDefaultConfig()
        .buildAsync();

    client.connectWith()
        .send();

    return client;
  }

}
