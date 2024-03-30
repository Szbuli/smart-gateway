import board
import busio
from adafruit_bme280 import advanced as adafruit_bme280
import mqtt_topics
import stat_util


class BME280:
    def __init__(self, mqtt):
        self.mqtt = mqtt
        i2c = busio.I2C(board.SCL, board.SDA)
        self.bme280 = adafruit_bme280.Adafruit_BME280_I2C(i2c, address=0x76)
        self.setConfig()

        self.temperature = self.bme280.temperature
        self.humidity = self.bme280.relative_humidity

    def setConfig(self):
        self.bme280.mode = adafruit_bme280.MODE_NORMAL
        self.bme280.standby_period = adafruit_bme280.STANDBY_TC_500
        self.bme280.iir_filter = adafruit_bme280.IIR_FILTER_X16
        self.bme280.overscan_humidity = adafruit_bme280.OVERSCAN_X1
        self.bme280.overscan_temperature = adafruit_bme280.OVERSCAN_X2

    def read(self):
        self.temperature = stat_util.smooth(
            self.bme280.temperature, self.temperature, 0.2)
        self.humidity = stat_util.smooth(
            self.bme280.relative_humidity, self.humidity, 0.2)

    def publish(self):
        self.mqtt.publishWithBaseTopic(
            mqtt_topics.bme280TemperatureTopic, str(round(self.temperature, 2)))
        self.mqtt.publishWithBaseTopic(mqtt_topics.bme280HumidityTopic, str(
            round(self.humidity, 2)))
