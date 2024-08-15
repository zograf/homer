import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device

class Car:
    def __init__(self, fill_to):
        self.capacity = random.random() * 20.0 + 30.0
        self.value = self.capacity * random.random() * fill_to / 100
        self.consumed = 0.0

    def add_value(self, delta):
        self.value += delta
        self.consumed += delta

    def get_percent(self):
        return self.value / self.capacity * 100
    
    def start_str(self):
        return "Capacity: %.2f kWh, Filled to: %.2f%%" % (self.capacity, self.get_percent())
    
    def stop_str(self):
        return "Capacity: %.2f kWh, Consumed: %.2f kWh" % (self.capacity, self.consumed)

class Charger(Device):
    def __init__(self, property_id, device_id, user_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.user_id = user_id
        self.fill_to = 100.0
        self.slots = [None for _ in range(3)]
        
    def generate_data(self):
        ret = []
        now = datetime.now()
        start = datetime.now() - timedelta(days=120)

        count = 0
        while (start <= now):

            online = self.is_online(start)
            if online[1] is not None: ret.append(online[1])

            if online[0]:
                point = self.change_fill(start)
                if point is not None:
                    ret.append(point)

            if online[0]:
                point = self.start_charging(start)
                if point is not None:
                    ret.append(point)

            if online[0] and count == 5:
                consumed = 0.0
                for i in range(3):
                    if self.slots[i] is not None:
                        self.slots[i].add_value(15.0 / 60)
                        consumed += 15.0 / 60
                        ret.append(Point("charging_progress")
                                   .time(start, "ms")
                                   .tag("propertyId", str(self.property_id))
                                   .tag("deviceId", str(self.device_id))
                                   .tag("slot", str(i))
                                   .field("value", self.slots[i].get_percent()))
                        if self.slots[i].get_percent() >= self.fill_to:
                            ret.append(Point("action")
                                .time(start, "ms")
                                .tag("type", "Stop charging")
                                .tag("propertyId", str(self.property_id))
                                .tag("deviceId", str(self.device_id))
                                .tag("slot", str(i))
                                .tag("userId", None)
                                .tag("userEmail", "(automatic)")
                                .tag("userName", "(automatic)")
                                .field("value", self.slots[i].stop_str()))
                            self.slots[i] = None

                slots = 0
                for i in range(3):
                    if self.slots[i] is not None:
                        slots += 1

                ret.append(Point("occupied_slots")
                           .time(start, "ms")
                           .tag("propertyId", self.property_id)
                           .tag("deviceId", self.device_id)
                           .field("value", slots))
                
                ret.append(Point("consumption")
                           .time(start, "ms")
                           .tag("type", "Consumption")
                           .tag("propertyId", str(self.property_id))
                           .tag("cityId", str(self.city_id))
                           .tag("deviceId", str(self.device_id))
                           .field("value", float(consumed)))

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

    def start_charging(self, time):
        if time.hour == 19:
            if random.randint(0, 6 * 60) < 2:
                for i in range(3):
                    if self.slots[i] is None:
                        self.slots[i] = Car(self.fill_to)
                        return (Point("action")
                                .time(time, "ms")
                                .tag("type", "Start charging")
                                .tag("propertyId", str(self.property_id))
                                .tag("deviceId", str(self.device_id))
                                .tag("slot", str(i))
                                .tag("userId", None)
                                .tag("userEmail", "(automatic)")
                                .tag("userName", "(automatic)")
                                .field("value", self.slots[i].start_str()))
        return None

    def change_fill(self, time):
        if time.hour != 19:
            if random.randint(0, 10000) >= 9999:
                self.fill_to = random.random() * 70 + 30
                return (Point("action")
                        .time(time, "ms")
                        .tag("type", "Fill to percent")
                        .tag("propertyId", str(self.property_id))
                        .tag("deviceId", str(self.device_id))
                        .tag("userId", str(self.user_id))
                        .tag("userEmail", "user@gmail.com")
                        .tag("userName", "User")
                        .field("value", "%.2f%%" % (self.fill_to)))
        return None