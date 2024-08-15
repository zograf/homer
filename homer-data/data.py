import json
import influxdb_client
import time
from influxdb_client.client.write_api import SYNCHRONOUS

from gate import Gate
from lamp import Lamp

from sensor import Sensor
from ac import AC
from sprinklers import Sprinklers
from wm import WM
from battery import Battery
from panel import Panel
from charger import Charger

class DataGenerator():
    def __init__(self):
        with open("settings.json", "r") as f:
            self.settings = json.load(f)

        self.bucket_name = self.settings["bucket"]
        self.org = self.settings["org"]
        self.url = self.settings["url"]
        self.token = self.settings["token"]

        self.client = influxdb_client.InfluxDBClient(url=self.url, token=self.token, org=self.org, timeout=90000)
        self.write_api = self.client.write_api(write_options=SYNCHRONOUS)
    
    def write(self, data):
        self.write_api.write(bucket=self.settings["bucket"], org=self.settings["org"], record=data)
    
    def delete_bucket(self):
        try:
            bucket = self.client.buckets_api().find_bucket_by_name(self.bucket_name)
            self.client.buckets_api().delete_bucket(bucket.id)
            print(f"Bucket '{self.bucket_name}' deleted successfully.")
        except Exception as e:
            print(f"Error deleting bucket '{self.bucket_name}': {e}")

    def create_bucket(self):
        try:
            self.client.buckets_api().create_bucket(bucket_name=self.bucket_name, org=self.org)
            print(f"Bucket '{self.bucket_name}' created successfully.")
        except Exception as e:
            print(f"Error creating bucket '{self.bucket_name}': {e}")

    def generate(self):
        start = time.time()
        self.delete_bucket()
        self.create_bucket()

        self.generate_property()

        self.settings["property_id"] += 1
        devices = ["sensor_device_id", "ac_device_id", "wm_device_id", "lamp_device_id", "gate_device_id",
                   "sprinklers_device_id", "panels_device_id", "battery_device_id", "charger_device_id"]
        for device in devices:
            self.settings[device] += 9

        self.generate_property()

        end = time.time()
        print(f"Time elapsed: {end-start} seconds")

    def generate_property(self):
        # COMMENT BELOW FOR TESTING
        self.generate_sensor()
        self.generate_ac()
        self.generate_wm()
        self.generate_lamp()
        self.generate_gate()
        self.generate_sprinklers()
        self.generate_panels()
        self.generate_battery()
        self.generate_charger()
    
    def generate_sensor(self):
        print(f"Writing SENSOR DATA to influxdb...")
        device = Sensor(self.settings["property_id"], self.settings["sensor_device_id"], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_ac(self):
        print(f"Writing AIR CONDITIONER DATA to influxdb...")
        device = AC(self.settings["property_id"], self.settings["ac_device_id"], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_wm(self):
        print(f"Writing WASHING MACHINE DATA to influxdb...")
        device = WM(self.settings["property_id"], self.settings["wm_device_id"], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_lamp(self):
        print(f"Writing LAMP DATA to influxdb...")
        device = Lamp(self.settings["property_id"], self.settings["lamp_device_id"], self.settings["user_id"], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_gate(self):
        print(f"Writing GATE DATA to influxdb...")
        device = Gate(self.settings["property_id"], self.settings["gate_device_id"], self.settings["user_id"], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_sprinklers(self):
        print(f"Writing SPRINKLERS DATA to influxdb...")
        device = Sprinklers(self.settings["property_id"], self.settings["sprinklers_device_id"], self.settings["user_id"], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_panels(self):
        print(f"Writing PANELS DATA to influxdb...")
        device = Panel(self.settings["property_id"], self.settings["panels_device_id"], self.settings['user_id'], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_battery(self):
        print(f"Writing BATTERY DATA to influxdb...")
        device = Battery(self.settings["property_id"], self.settings["battery_device_id"], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

    def generate_charger(self):
        print(f"Writing CHARGER DATA to influxdb...")
        device = Charger(self.settings["property_id"], self.settings["charger_device_id"], self.settings['user_id'], self.settings["city_id"])
        points = device.generate_data()
        self.write(points)
        print(f"Wrote {len(points)} points to influxdb")

if __name__ == "__main__":
    generator = DataGenerator()
    generator.generate()