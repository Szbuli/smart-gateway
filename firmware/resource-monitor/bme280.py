import board
import busio
from adafruit_bme280 import advanced as adafruit_bme280


class BME280:
    def __init__(self, mqtt, temperatureTopic, humidityTopic):
        self.temperatureTopic = temperatureTopic
        self.humidityTopic = humidityTopic
        self.mqtt = mqtt
        i2c = busio.I2C(board.SCL, board.SDA)
        self.bme280 = adafruit_bme280.Adafruit_BME280_I2C(i2c, address=0x76)
        self.setConfig()

    def setConfig(self):
        self.bme280.mode = adafruit_bme280.MODE_NORMAL
        self.bme280.standby_period = adafruit_bme280.STANDBY_TC_500
        self.bme280.iir_filter = adafruit_bme280.IIR_FILTER_X16
        self.bme280.overscan_humidity = adafruit_bme280.OVERSCAN_X1
        self.bme280.overscan_temperature = adafruit_bme280.OVERSCAN_X2

    def read_and_publish(self):
        self.mqtt.publish(self.temperatureTopic, str(
            round(self.bme280.temperature, 2)))
        self.mqtt.publish(self.humidityTopic, str(
            round(self.bme280.relative_humidity, 2)))
