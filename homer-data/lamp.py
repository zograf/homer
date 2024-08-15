import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device


class Lamp(Device):

    def __init__(self, property_id, device_id, user_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.user_id = user_id
        self.auto = True
        self.is_on = False

    def generate_data(self):
        ret = []
        now = datetime.now()
        current = datetime.now() - timedelta(days=210)
        prev_hour = -1

        while (current <= now):

            online = self.is_online(current)
            if online[1] is not None: ret.append(online[1])

            if online[0]:
                consumed = self.consume(current)
                if consumed is not None:
                    ret.append(consumed)

                auto_brightness = self.get_auto_brightness_point(current)
                if auto_brightness is not None: ret.append(auto_brightness)

                light_pressence = self.get_light_pressence(current.month - 1, current.hour, current.minute)
                ret.append(Point("light_presence")
                            .time(current, "ms")
                            .tag("type", "Presence")
                            .tag("propertyId", str(self.property_id))
                            .tag("deviceId", str(self.device_id))
                            .field("value", light_pressence))

                auto_on_off = self.get_auto_on_off_point(current, light_pressence)
                if auto_on_off is not None: ret.append(auto_on_off)

                if prev_hour != current.hour:
                    prev_hour = current.hour
                    user_on_off = self.get_random_user_point(current)
                    if user_on_off is not None: ret.append(user_on_off)

            current += timedelta(seconds=10)

        return ret


    def get_light_pressence(self, month, hour, minute):
        amounts = [
            [  0,   5,   5,   9,  11,  11,  15,  60,  85,  95, 100, 100, 100, 100, 100, 100,  85,  35,  10,   8,   5,   3,   1,   0],
            [  0,   5,   5,   9,  11,  11,  35,  65,  85,  95, 100, 100, 100, 100, 100, 100,  95,  55,  12,   8,   5,   3,   1,   0],
            [  0,   5,   5,   9,  11,  11,  45,  75,  85,  95, 100, 100, 100, 100, 100, 100, 100,  85,  32,   8,   5,   3,   1,   0],
            [  0,   5,   5,   9,  11,  24,  55,  81,  89,  95, 100, 100, 100, 100, 100, 100, 100,  95,  42,  28,   5,   3,   1,   0],
            [  0,   5,   5,   9,  16,  34,  62,  81,  95,  99, 100, 100, 100, 100, 100, 100, 100, 100,  68,  48,  25,   3,   1,   0],
            [  0,   5,   5,   9,  16,  34,  72,  91,  98, 100, 100, 100, 100, 100, 100, 100, 100, 100,  82,  68,  45,  23,  10,   0],
            [  0,   5,   5,   9,  16,  34,  72,  95, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,  92,  88,  58,  33,  12,   0],
            [  0,   5,   5,   9,  16,  34,  72,  95, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,  94,  68,  43,  20,   0],
            [  0,   5,   5,   9,  16,  34,  72,  91,  98, 100, 100, 100, 100, 100, 100, 100, 100, 100,  82,  68,  45,  23,  10,   0],
            [  0,   5,   5,   9,  11,  24,  55,  81,  89,  95, 100, 100, 100, 100, 100, 100, 100,  95,  42,  28,   5,   3,   1,   0],
            [  0,   5,   5,   9,  11,  11,  35,  65,  85,  95, 100, 100, 100, 100, 100,  95,  85,  55,  12,   8,   5,   3,   1,   0],
            [  0,   5,   5,   9,  11,  11,  15,  50,  75,  85,  99, 100, 100, 100,  99,  88,  75,  35,  10,   8,   5,   3,   1,   0]
        ]

        base_light_min = amounts[month][hour]
        base_light_max = amounts[month][(hour + 1) % 24]

        return base_light_min + (base_light_max - base_light_min) / 60 * minute


    def get_auto_brightness_point(self, timestamp):
        if timestamp.month % 2 == 0 and not self.auto:
            self.auto = True
            return (Point("action")
                       .time(timestamp, "ms")
                       .tag("type", "Auto Brightness")
                       .tag("propertyId", str(self.property_id))
                       .tag("deviceId", str(self.device_id))
                       .tag("userId", self.user_id)
                       .tag("userEmail", "user@gmail.com")
                       .tag("userName", "User")
                       .field("enabled", "Enabled"))

        elif timestamp.month % 2 != 0 and self.auto:
            self.auto = False
            return (Point("action")
                       .time(timestamp, "ms")
                       .tag("type", "Auto Brightness")
                       .tag("propertyId", str(self.property_id))
                       .tag("deviceId", str(self.device_id))
                       .tag("userId", self.user_id)
                       .tag("userEmail", "user@gmail.com")
                       .tag("userName", "User")
                       .field("enabled", "Disabled"))

        else: return None


    def get_auto_on_off_point(self, timestamp, level):
        if not self.auto: return None

        if level > 50.0 and self.is_on: self.is_on = False
        elif level < 50.0 and not self.is_on: self.is_on = True
        else: return None

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
        if self.auto: return None

        if random.randint(0, 100) > 50: self.is_on = not self.is_on
        else: return None

        return (Point("action")
                .time(timestamp, "ms")
                .tag("type", "Turn on/off")
                .tag("propertyId", str(self.property_id))
                .tag("deviceId", str(self.device_id))
                .tag("userId", self.user_id)
                .tag("userEmail", "user@gmail.com")
                .tag("userName", "User")
                .field("on", "On" if self.is_on else "Off"))