import threading
import time
import RPi.GPIO as GPIO
import mqtt_topics

tamper_pin = 17


class Tamper:
    def __init__(self, mqtt):
        self.lock = threading.Lock()
        self.mqtt = mqtt
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(tamper_pin, GPIO.IN, pull_up_down=GPIO.PUD_OFF)
        GPIO.add_event_detect(
            tamper_pin, GPIO.BOTH, callback=self.tamper_event)
        self.tamper_event_happened = None

    def tamper_event(self, channel):
        if self.lock.locked():
            try:
                self.lock.release()
            except:
                pass

    def start(self):
        t = threading.Thread(target=self.check_tamper)
        t.setDaemon(True)
        t.start()

    def check_tamper(self):
        while True:
            self.lock.acquire()
            checks = 0
            while checks < 5:
                checks += 1
                tamper_state = GPIO.input(tamper_pin)
                time.sleep(0.3)
                if self.tamper_event_happened != tamper_state:
                    self.tamper_event_happened = tamper_state
                    self.mqtt.publishWithBaseTopic(mqtt_topics.tamperTopic,
                                                   "OFF" if tamper_state == 1 else "ON")
