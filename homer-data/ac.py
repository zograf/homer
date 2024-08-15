import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device


class AC(Device):
    def __init__(self, property_id, device_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.temperature_min = 15
        self.temperature_max = 30
        self.temperature = 20
        self.modes = ["AUTOMATIC", "COOLING", "HEATING"]
        self.mode = self.modes[0]
        self.isOn = False
        self.isUser = False

    def generate_data(self):
        ret = []
        now = datetime.now()
        start = datetime.now() - timedelta(days=180)
        while (start <= now):

            online = self.is_online(start)
            if online[1] is not None: ret.append(online[1])

            if online[0]:
                if (self.isOn):
                    consumed = self.consume(start)
                    if consumed is not None:
                        ret.append(consumed)

                    ret.append(Point("action")
                            .time(start, "ms")
                            .tag("type", "Temperature changed")
                            .tag("propertyId", str(self.property_id))
                            .tag("deviceId", str(self.device_id))
                            .tag("userId", None)
                            .tag("userEmail", "(automatic)" if not self.isUser else "user@gmail.com")
                            .tag("userName", "(automatic)" if not self.isUser else "User")
                            .field("temperature", str(self.temperature)))

                    ret.append(Point("action")
                            .time(start, "ms")
                            .tag("type", "Mode changed")
                            .tag("propertyId", str(self.property_id))
                            .tag("deviceId", str(self.device_id))
                            .tag("userId", None)
                            .tag("userEmail", "(automatic)" if not self.isUser else "user@gmail.com")
                            .tag("userName", "(automatic)" if not self.isUser else "User")
                            .field("mode", str(self.mode)))

                    if random.randint(0, 100) > 90:
                       self.isOn = False
                       ret.append(Point("action")
                                .time(start, "ms")
                                .tag("type", "Turn on/off")
                                .tag("propertyId", str(self.property_id))
                                .tag("deviceId", str(self.device_id))
                                .tag("userId", None)
                                .tag("userEmail", "(automatic)" if not self.isUser else "user@gmail.com")
                                .tag("userName", "(automatic)" if not self.isUser else "User")
                                .field("on", "On" if self.isOn else "Off"))
                else:
                    if random.randint(0, 100) > 90:
                       self.isOn = True
                       ret.append(Point("action")
                                .time(start, "ms")
                                .tag("type", "Turn on/off")
                                .tag("propertyId", str(self.property_id))
                                .tag("deviceId", str(self.device_id))
                                .tag("userId", None)
                                .tag("userEmail", "(automatic)" if not self.isUser else "user@gmail.com")
                                .tag("userName", "(automatic)" if not self.isUser else "User")
                                .field("on", "On" if self.isOn else "Off"))

                if random.randint(0, 100) > 20:
                    self.temperature = random.randint(self.temperature_min, self.temperature_max)

                if random.randint(0, 100) > 80:
                    self.mode = random.choice(self.modes)

                if random.randint(0, 100) > 50:
                    self.isUser = not self.isUser

            start += timedelta(minutes=10)
        return ret