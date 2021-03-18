package hu.szbuli.smarthome.rpi.mqtt.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hu.szbuli.smarthome.can.CanReceiveThread;
import hu.szbuli.smarthome.can.CanSendThread;
import hu.szbuli.smarthome.gateway.heartbeat.HeartBeatService;
import hu.szbuli.smarthome.gateway.homeassistant.DeviceType;
import hu.szbuli.smarthome.gateway.homeassistant.DiscoveryManager;
import hu.szbuli.smarthome.mqtt.MqttManager;

public class Main {

  public static void main(String[] args) throws IOException, ParseException {
    Options options = new Options();
    options.addOption("m", "mqtt", true, "mqtt config file");
    options.addOption("g", "gateway", true, "gateway config file");
    options.addOption("d", "deviceTypes", true, "device types config file");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    final String mqttConfigFile = cmd.getOptionValue("m", "./mqtt.properties");
    final String gatewayConfigFile = cmd.getOptionValue("g", "./config.csv");
    final String deviceTypesConfigFile = cmd.getOptionValue("d", "./deviceTypes.json");

    Properties prop = new Properties();
    prop.load(new FileInputStream(mqttConfigFile));

    String healthStatusTopic = prop.getProperty("statusTopic");

    MqttConfiguration mqttConfiguration = new MqttConfiguration();
    mqttConfiguration.setHost(prop.getProperty("host"));
    mqttConfiguration.setPort(Integer.parseInt(prop.getProperty("port")));
    mqttConfiguration.setUsername(prop.getProperty("username"));
    mqttConfiguration.setPassword(prop.getProperty("password"));

    MqttManager mqttManager = new MqttManager(mqttConfiguration, healthStatusTopic);
    mqttManager.connect(true, true);

    DeviceType[] deviceTypes = parseDeviceTypes(deviceTypesConfigFile);
    DiscoveryManager discoveryManager = new DiscoveryManager(mqttManager, deviceTypes, prop.getProperty("gatewayName"));
    HeartBeatService heartBeatService = new HeartBeatService(mqttConfiguration);
    heartBeatService.start();

    Gateway gateway = new Gateway(gatewayConfigFile, mqttManager, discoveryManager, heartBeatService, CanSendThread.sendCanQueue);

    CanSendThread canSendThread = new CanSendThread();
    canSendThread.start();

    CanReceiveThread canRecieveThread = new CanReceiveThread(gateway);
    canRecieveThread.start();
  }

  private static DeviceType[] parseDeviceTypes(String deviceTypesConfigFile) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(Paths.get(deviceTypesConfigFile).toFile(), DeviceType[].class);
  }

}
