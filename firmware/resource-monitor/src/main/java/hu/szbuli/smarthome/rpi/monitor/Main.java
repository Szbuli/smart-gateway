package hu.szbuli.smarthome.rpi.monitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import hu.szbuli.smarthome.rpi.monitor.mqtt.MqttManager;

public class Main {

  private static final int READ_FREQUENCY_SECONDS = 1;

  public static void main(String[] args) throws UnsupportedBusNumberException, IOException, InterruptedException, ParseException {

    Options options = new Options();
    options.addOption("m", "mqtt", true, "mqtt config file");
    options.addOption("g", "gateway", true, "gateway config file");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    final String mqttConfigFile = cmd.getOptionValue("m", "./mqtt.properties");
    Properties props = new Properties();
    props.load(new FileInputStream(mqttConfigFile));

    MqttManager mqttManager = new MqttManager();
    mqttManager.init(props);

    Tamper tamper = new Tamper(props, mqttManager);
    tamper.init();

    Ads1X15 ads1x15 = new Ads1X15(props, mqttManager);
    ads1x15.init();

    Bme280 bme280 = new Bme280(props, mqttManager);
    bme280.init();

    // Output data to screen

    while (true) {
      ads1x15.getValue();
      bme280.getValue();
      Thread.sleep(READ_FREQUENCY_SECONDS * 60 * 1000);
    }

  }

}
