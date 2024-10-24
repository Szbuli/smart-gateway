import board
import logging
import busio
from adafruit_ina219 import ADCResolution, BusVoltageRange, INA219 as LIB_INA219
import mqtt_topics
import stat_util


class INA219:
    def __init__(self, mqtt):
        self.mqtt = mqtt
        i2c = busio.I2C(board.SCL, board.SDA)
        self.ina219 = LIB_INA219(i2c)

        if self.ina219.overflow:
            print("Internal Math Overflow Detected!")

        self.ina219.bus_adc_resolution = ADCResolution.ADCRES_12BIT_128S
        self.ina219.shunt_adc_resolution = ADCResolution.ADCRES_12BIT_128S
        self.ina219.bus_voltage_range = BusVoltageRange.RANGE_16V

        self.bus_voltage = self.ina219.bus_voltage
        self.current = self.ina219.current
        self.power = self.ina219.power

    def read(self):
        self.bus_voltage = stat_util.smooth(
            self.ina219.bus_voltage, self.bus_voltage, 0.2)
        self.current = stat_util.smooth(self.ina219.current, self.current, 0.2)
        self.power = stat_util.smooth(self.ina219.power, self.power, 0.2)

    def publish(self):
        self.mqtt.publishWithBaseTopic(
            mqtt_topics.ina219VoltageTopic, str(round(self.bus_voltage, 2)))
        self.mqtt.publishWithBaseTopic(
            mqtt_topics.ina219CurrentTopic, str(round(self.current / 1000, 2)))
        self.mqtt.publishWithBaseTopic(
            mqtt_topics.ina219PowerTopic, str(round(self.power, 2)))
