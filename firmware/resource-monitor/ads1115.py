import board
import logging
import busio
import adafruit_ads1x15.ads1115 as ADS
from adafruit_ads1x15.analog_in import AnalogIn


class ADS1115:
    def __init__(self, mqtt, voltageTopic, voltageMultiplier):
        self.voltageTopic = voltageTopic
        self.mqtt = mqtt
        self.voltageMultiplier = float(voltageMultiplier)
        i2c = busio.I2C(board.SCL, board.SDA)
        self.ads = ADS.ADS1115(i2c, data_rate=8)
        self.chan = AnalogIn(self.ads, ADS.P0)

    def read_and_publish(self):
        self.mqtt.publish(self.voltageTopic, str(
            round(self.chan.voltage * self.voltageMultiplier, 2)))
