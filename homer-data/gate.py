import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device


class Gate(Device):

    def __init__(self, property_id, device_id, user_id, city_id):
        super().__init__(property_id, device_id, city_id)
        self.user_id = user_id
        self.is_open = False
        self.is_private = False
        self.last_interaction = None
        self.private_plates_dyn = ["SO064AJ", "SO169RK"]
        self.private_plates = list(self.private_plates_dyn)
        self.public_plates_dyn = self.__generate_random_plate(10)
        self.public_plates = list(self.public_plates_dyn)
        self.parked = []


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

                is_private_change_point = self.get_private_public_point(current)
                if is_private_change_point is not None: ret.append(is_private_change_point)

                if self.last_interaction is None or (current - self.last_interaction).total_seconds() / 60 >= 60:
                    self.last_interaction = current

                    enter_vs_exit = random.randint(0, 1)
                    if enter_vs_exit == 1 and len(self.parked) > 0:
                        # Vehicle Leaves
                        plate_to_leave = self.parked[random.randint(0, len(self.parked) - 1)]
                        self.parked.remove(plate_to_leave)
                        ret.append((Point("action")
                                    .time(current, "ms")
                                    .tag("type", "Open/Close")
                                    .tag("propertyId", str(self.property_id))
                                    .tag("deviceId", str(self.device_id))
                                    .tag("userId", None)
                                    .tag("userEmail", "(automatic)")
                                    .tag("userName", "(automatic)")
                                    .field("openClose", "Opened")))
                        ret.append((Point("action")
                                   .time(current + timedelta(seconds=1), "ms")
                                   .tag("type", "Presence")
                                   .tag("propertyId", str(self.property_id))
                                   .tag("deviceId", str(self.device_id))
                                   .tag("userId", None)
                                   .tag("userEmail", "(automatic)")
                                   .tag("userName", plate_to_leave)
                                   .field("enterExit", "Exit")))
                        ret.append((Point("action")
                                    .time(current + timedelta(seconds=6), "ms")
                                    .tag("type", "Open/Close")
                                    .tag("propertyId", str(self.property_id))
                                    .tag("deviceId", str(self.device_id))
                                    .tag("userId", None)
                                    .tag("userEmail", "(automatic)")
                                    .tag("userName", "(automatic)")
                                    .field("openClose", "Closed")))
                        if plate_to_leave in self.public_plates: self.public_plates_dyn.append(plate_to_leave)
                        else: self.private_plates_dyn.append(plate_to_leave)

                    else:
                        private_vs_public = random.randint(0, 1)
                        plate_to_enter = None

                        if not self.is_private and private_vs_public == 1 and len(self.public_plates_dyn) > 0:
                            plate_to_enter = self.public_plates_dyn[random.randint(0, len(self.public_plates_dyn) - 1)]
                            self.public_plates_dyn.remove(plate_to_enter)

                        elif self.is_private and private_vs_public == 1 and len(self.public_plates_dyn) > 0:
                            plate_to_enter = self.public_plates_dyn[random.randint(0, len(self.public_plates_dyn) - 1)]
                            ret.append((Point("action")
                                        .time(current + timedelta(seconds=1), "ms")
                                        .tag("type", "Presence")
                                        .tag("propertyId", str(self.property_id))
                                        .tag("deviceId", str(self.device_id))
                                        .tag("userId", None)
                                        .tag("userEmail", "(automatic)")
                                        .tag("userName", plate_to_enter)
                                        .field("enterExit", "Tried To Enter")))
                            plate_to_enter = None

                        elif private_vs_public == 0 and len(self.private_plates_dyn) > 0:
                            plate_to_enter = self.private_plates_dyn[random.randint(0, len(self.private_plates_dyn) - 1)]
                            self.private_plates_dyn.remove(plate_to_enter)
                            pass

                        if plate_to_enter is not None:
                            self.parked.append(plate_to_enter)
                            ret.append((Point("action")
                                        .time(current, "ms")
                                        .tag("type", "Open/Close")
                                        .tag("propertyId", str(self.property_id))
                                        .tag("deviceId", str(self.device_id))
                                        .tag("userId", None)
                                        .tag("userEmail", "(automatic)")
                                        .tag("userName", "(automatic)")
                                        .field("openClose", "Opened")))
                            ret.append((Point("action")
                                        .time(current + timedelta(seconds=1), "ms")
                                        .tag("type", "Presence")
                                        .tag("propertyId", str(self.property_id))
                                        .tag("deviceId", str(self.device_id))
                                        .tag("userId", None)
                                        .tag("userEmail", "(automatic)")
                                        .tag("userName", plate_to_enter)
                                        .field("enterExit", "Enter")))
                            ret.append((Point("action")
                                        .time(current + timedelta(seconds=6), "ms")
                                        .tag("type", "Open/Close")
                                        .tag("propertyId", str(self.property_id))
                                        .tag("deviceId", str(self.device_id))
                                        .tag("userId", None)
                                        .tag("userEmail", "(automatic)")
                                        .tag("userName", "(automatic)")
                                        .field("openClose", "Closed")))

            current += timedelta(seconds=10)

        return ret

    def get_private_public_point(self, timestamp):
        if timestamp.weekday() <= 4 and self.is_private:
            self.is_private = False
            return (Point("action")
                       .time(timestamp, "ms")
                       .tag("type", "Public/Private")
                       .tag("propertyId", str(self.property_id))
                       .tag("deviceId", str(self.device_id))
                       .tag("userId", self.user_id)
                       .tag("userEmail", "user@gmail.com")
                       .tag("userName", "User")
                       .field("enabled", "Changed to Public"))

        elif timestamp.weekday() > 4 and not self.is_private:
            self.is_private = True
            return (Point("action")
                       .time(timestamp, "ms")
                       .tag("type", "Public/Private")
                       .tag("propertyId", str(self.property_id))
                       .tag("deviceId", str(self.device_id))
                       .tag("userId", self.user_id)
                       .tag("userEmail", "user@gmail.com")
                       .tag("userName", "User")
                       .field("enabled", "Changed to Private"))

        else: return None


    @staticmethod
    def __generate_random_plate(count):
        plates = []
        alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
        for i in range(0, count):
            plate = ""
            plate += alphabet[random.randint(0, len(alphabet) - 1)]
            plate += alphabet[random.randint(0, len(alphabet) - 1)]
            plate += str(random.randint(0, 9))
            plate += str(random.randint(0, 9))
            plate += str(random.randint(0, 9))
            plate += alphabet[random.randint(0, len(alphabet) - 1)]
            plate += alphabet[random.randint(0, len(alphabet) - 1)]
            plates.append(plate)
        return plates