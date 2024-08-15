import random
from datetime import datetime, timedelta
from influxdb_client import Point

from device import Device


class Sensor(Device):
    def __init__(self, property_id, device_id, city_id):
        super().__init__(property_id, device_id, city_id)

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

                temperature = self.get_temperature(start.month, start.hour)
                humidity = self.get_humidity(start.month, start.hour)

                ret.append(Point("ambient_sensor_temperature")
                           .time(start, "ms")
                           .tag("propertyId", str(self.property_id))
                           .tag("deviceId", str(self.device_id))
                           .field("temperature", temperature))

                ret.append(Point("ambient_sensor_humidity")
                           .time(start, "ms")
                           .tag("propertyId", str(self.property_id))
                           .tag("deviceId", str(self.device_id))
                           .field("humidity", humidity))

            start += timedelta(seconds=10)
        return ret

    def get_temperature(self, month, hour):
        base_temp = 30.0
        if (month < 3 or month > 10):
            base_temp = 10.0
        elif (month < 6 or month > 8):
            base_temp = 20.0
        
        modifier = -2
        if ((hour > 6 and hour < 10) or (hour > 18 and hour < 23)):
            modifier = 1
        elif (hour >= 10 and hour <= 18):
            modifier = 3

        return base_temp + modifier * random.random() / 2

    def get_humidity(self, month, hour):
        base_humidity = 30.0
        if (month < 3 or month > 10):
            base_humidity = 10.0
        elif (month < 6 or month > 8):
            base_humidity = 20.0

        modifier = -2
        if ((hour > 6 and hour < 10) or (hour > 18 and hour < 23)):
            modifier = 1
        elif (hour >= 10 and hour <= 18):
            modifier = 3

        return base_humidity + 15 + random.random() + modifier