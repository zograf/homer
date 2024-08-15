import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device


class Sprinklers(Device):

    def __init__(self, property_id, device_id, user_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.user_id = user_id
        self.is_on = False
        self.last_user_interaction = None


    def generate_data(self):
        ret = []
        now = datetime.now()
        current = datetime.now() - timedelta(days=210)

        while (current <= now):

            online = self.is_online(current)
            if online[1] is not None: ret.append(online[1])

            if online[0]:
                consumed = self.consume(current)
                if consumed is not None:
                    ret.append(consumed)
                
                if (current.weekday() == 1 or current.weekday() == 4) and current.hour == 17 and current.minute == 0 and not self.is_on: ret.append(self.get_auto_on_off_point(current, True))
                elif (current.weekday() == 1 or current.weekday() == 4) and current.hour == 17 and current.minute == 30 and self.is_on: ret.append(self.get_auto_on_off_point(current, False))
                elif current.weekday() != 1 and current.weekday() != 4:
                    if self.last_user_interaction is None: self.last_user_interaction = current
                    user_on_off = self.get_random_user_point(current)
                    if user_on_off is not None: ret.append(user_on_off)

            current += timedelta(seconds=10)

        return ret


    def get_auto_on_off_point(self, timestamp, is_on):
        self.is_on = is_on
        return (Point("action")
                .time(timestamp, "ms")
                .tag("type", "Turn on/off")
                .tag("propertyId", str(self.property_id))
                .tag("deviceId", str(self.device_id))
                .tag("userId", None)
                .tag("userEmail", "(automatic)")
                .tag("userName", "(automatic)")
                .field("on", "On" if self.is_on else "Off"))


    def get_random_user_point(self, timestamp):

        if not self.is_on and ((timestamp - self.last_user_interaction).total_seconds() / 60 / 60) >= 72 and random.randint(0, 100) >= 98: self.is_on = True
        elif self.is_on and ((timestamp - self.last_user_interaction).total_seconds() / 60) >= 20: self.is_on = False
        else: return None

        self.last_user_interaction = timestamp
        return (Point("action")
                .time(timestamp, "ms")
                .tag("type", "Turn on/off")
                .tag("propertyId", str(self.property_id))
                .tag("deviceId", str(self.device_id))
                .tag("userId", self.user_id)
                .tag("userEmail", "user@gmail.com")
                .tag("userName", "User")
                .field("on", "On" if self.is_on else "Off"))
