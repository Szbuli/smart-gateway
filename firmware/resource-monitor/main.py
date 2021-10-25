import time
import logging
import configparser
from rpi_status import RpiStatus
from bme280 import BME280
from tamper import Tamper
from ina219 import INA219
from mqtt_manager import MqttManager
from ha_discovery import HaDiscovery

from timeloop import Timeloop
from datetime import timedelta

tl = Timeloop()
rpi_status = None
bme_280 = None
tamper = None
ina219 = None


def start():
    config = configparser.ConfigParser()
    config.read('settings.ini')

    logging.basicConfig(level=logging.getLevelName(
        config['logger']['logLevel']))

    deviceName = config['rpi']['deviceName']

    mqtt_manager = MqttManager(config['mqtt']['host'],
                               config['mqtt']['port'],
                               config['mqtt']['username'],
                               config['mqtt']['password'],
                               deviceName,
                               config['mqtt']['baseTopic'])

    ha_discovery = HaDiscovery(mqtt_manager, deviceName)
    ha_discovery.registerSelfConnectivity()

    global rpi_status
    rpi_status = RpiStatus(mqtt_manager)
    ha_discovery.registerRpiStatus()

    if config.getboolean('sensors', 'bme280'):
        global bme_280
        bme_280 = BME280(mqtt_manager)
        ha_discovery.registerTemperature()

    if config.getboolean('sensors', 'tamper'):
        global tamper
        tamper = Tamper(mqtt_manager)
        tamper.start()
        ha_discovery.registerTamper()

    if config.getboolean('sensors', 'ina219'):
        global ina219
        ina219 = INA219(mqtt_manager)
        ha_discovery.registerPower()

    tl.start()
    publish_loop()

    mqtt_manager.loop()


@tl.job(interval=timedelta(seconds=5))
def read_loop():
    rpi_status.read()

    if bme_280 is not None:
        bme_280.read()

    if ina219 is not None:
        ina219.read()


@tl.job(interval=timedelta(seconds=60))
def publish_loop():
    rpi_status.publish()

    if bme_280 is not None:
        bme_280.publish()

    if ina219 is not None:
        ina219.publish()


if __name__ == '__main__':
    start()
