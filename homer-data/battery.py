import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device


class Battery(Device):
    def __init__(self, property_id, device_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.capacity = 100.0
        self.value = 0.0

    def generate_data(self):
        ret = []
        now = datetime.now()
        start = datetime.now() - timedelta(days=120)

        while (start <= now):

            online = self.is_online(start)
            if online[1] is not None: ret.append(online[1])

            if online[0]:
                value = self.get_battery_value(start)
                
                ret.append(Point("battery_state")
                           .time(start, "ms")
                           .tag("propertyId", str(self.property_id))
                           .tag("deviceId", str(self.device_id))
                           .field("value", float(value)))
                
                delta = self.get_delta(start)

                ret.append(Point("electricity_distribution")
                           .time(start, "ms")
                           .tag("cityId", str(self.city_id))
                           .tag("propertyId", str(self.property_id))
                           .tag("deviceId", str(self.device_id))
                           .field("delta", float(delta)))

            start += timedelta(seconds=10)
        return ret

    def get_battery_value(self, time):
        if time.hour >= 10 and time.hour <= 15:
            self.value += random.random()
            self.value = min(self.value, self.capacity)
        else:
            self.value -= random.random() * 0.5
            self.value = max(self.value, 0)
        return self.value
    
    def get_delta(self, time):
        if time.hour >= 11 and time.hour <= 14:
            return 1.0 / 60 + random.random() * 0.001
        elif time.hour >= 10 and time.hour <= 15:
            return 0
        return -9.0 / 60 + random.random() * 0.01