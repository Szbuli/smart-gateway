import board
import logging
import busio
from adafruit_ina219 import ADCResolution, BusVoltageRange, INA219 as LIB_INA219
import mqtt_topics


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

    def read_and_publish(self):
        bus_voltage = self.ina219.bus_voltage
        current = self.ina219.current
        power = self.ina219.power
        self.mqtt.publishWithBaseTopic(
            mqtt_topics.ina219VoltageTopic, str(round(bus_voltage, 2)))
        self.mqtt.publishWithBaseTopic(
            mqtt_topics.ina219CurrentTopic, str(round(current / 1000, 2)))
        self.mqtt.publishWithBaseTopic(
            mqtt_topics.ina219PowerTopic, str(round(power, 2)))
