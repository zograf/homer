import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device

class WM(Device):
    def __init__(self, property_id, device_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.modes = [5, 30, 120, 180]
        self.mode_names = {5: "Spin", 30: "Rinse", 120: "40 degree wash", 180: "90 degree wash"}
        self.mode = self.modes[3]
        self.isOn = False
        self.isUser = False

    def generate_data(self):
        ret = []
        now = datetime.now()
        start = datetime.now() - timedelta(days=120)
        while (start <= now):
            online = self.is_online(start)
            if online[1] is not None: ret.append(online[1])

            if online[0]:
                consumed = self.consume(start)
                if consumed is not None:
                    ret.append(consumed)
                
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

                    start += timedelta(seconds=2)

                    ret.append(Point("action")
                            .time(start, "ms")
                            .tag("type", "Mode changed")
                            .tag("propertyId", str(self.property_id))
                            .tag("deviceId", str(self.device_id))
                            .tag("userId", None)
                            .tag("userEmail", "(automatic)" if not self.isUser else "user@gmail.com")
                            .tag("userName", "(automatic)" if not self.isUser else "User")
                            .field("mode", str(self.mode_names[self.mode])))

                    start += timedelta(minutes=self.mode)

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

                    self.mode = random.choice(self.modes)
                    if random.randint(0, 100) > 50:
                        self.isUser = not self.isUser

                    start += timedelta(minutes=10)
            start += timedelta(seconds=10)
        return ret