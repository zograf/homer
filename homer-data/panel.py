import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device


class Panel(Device):
    def __init__(self, property_id, device_id, user_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.user_id = user_id
        self.is_on = True
        self.off_time_remaining = 0
        
    def generate_data(self):
        ret = []
        now = datetime.now()
        start = datetime.now() - timedelta(days=120)

        count = 0

        while (start <= now):

            online = self.is_online(start)
            if online[1] is not None: ret.append(online[1])

            if online[0]:
                point = self.on_off(start)
                if point is not None:
                    ret.append(point)

            if online[0] and self.is_on and count == 5:
                produced = self.get_produced_energy(start)
                
                ret.append(Point("consumption")
                           .time(start, "ms")
                           .tag("type", "Production")
                           .tag("propertyId", str(self.property_id))
                           .tag("cityId", str(self.city_id))
                           .tag("deviceId", str(self.device_id))
                           .field("value", float(-produced)))

            start += timedelta(seconds=10)
            count += 1
            if count == 6:
                count = 0

        return ret

    def get_produced_energy(self, time):
        if time.hour >= 10 and time.hour <= 15:
            return 1.0 / 60
        elif time.hour < 7 or time.hour > 19:
            return 0.0
        else:
            return 0.01 / 60
        
    def get_action(self, time):
        return Point("action") \
            .time(time, "ms") \
            .tag("type", "Turn on/off") \
            .tag("propertyId", str(self.property_id)) \
            .tag("userId", str(self.user_id)) \
            .tag("deviceId", str(self.device_id)) \
            .tag("userEmail", "user@gmail.com") \
            .tag("userName", "User") \
            .field("on", "On" if self.is_on else "Off")
        
    def on_off(self, time):
        if not self.is_on:
            self.off_time_remaining -= 10
            if self.off_time_remaining <= 0:
                self.off_time_remaining = 0
                self.is_on = True
                return self.get_action(time)
            return None

        off_chance = random.randint(0, 10000)
        if off_chance < 9999: return

        self.is_on = False
        self.off_time_remaining = random.randint(120, 1800)
        return self.get_action(time)
