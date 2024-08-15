import random
from abc import ABC
from datetime import datetime, timedelta

from influxdb_client import Point


class Device(ABC):

    def __init__(self, property_id, device_id, city_id):
        self.property_id = property_id
        self.device_id = device_id
        self.online = True
        self.offline_time_remaining = 0
        self.last_tick = datetime.now()
        self.city_id = city_id
        self.time_until_consume = 60


    def __get_status_point(self, timestamp):
        return (Point("online")
                        .time(timestamp, "ms")
                        .tag("propertyId", str(self.property_id))
                        .tag("deviceId", str(self.device_id))
                        .field("online", 1.0 if self.online else 0.0))

    def is_online(self, timestamp, delta=10):

        # If it's already offline check if it should come online
        if not self.online:
            self.offline_time_remaining -= delta
            if self.offline_time_remaining <= 0:
                self.offline_time_remaining = 0
                self.online = True
                return True, self.__get_status_point(timestamp)
            else: self.last_tick = timestamp
            return self.online, None

        # If online check if it should go offline
        offline_chance = random.randint(0, 10000)
        if offline_chance < 9999: return True, None

        # Change to offline mode
        self.online = False
        self.offline_time_remaining = random.randint(120, 1800)

        return False, self.__get_status_point(timestamp)

    def consume(self, timestamp):
        self.time_until_consume -= 10
        if self.time_until_consume == 0:
            self.time_until_consume = 60
            value = random.random() * 0.001 + 1.0 / 60
            return (Point("consumption")
                            .time(timestamp, "ms")
                            .tag("type", "Consumption")
                            .tag("propertyId", str(self.property_id))
                            .tag("cityId", str(self.city_id))
                            .tag("deviceId", str(self.device_id))
                            .field("value", value))
        else:
            return None