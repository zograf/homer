<img src="homer.png" width="128" height="128">

# Homer

Homer is a smart home web application that monitors and controlls your smart appliances inside of your smart homes.

# How to run
- Docker-compose the compose file inside the docker folder. You should have a basic mqtt config with `max_inflight_messages` set
- Run the `data.py` script from the homer-data. Create a settings.json file before running and specify the device ids, as well as other needed data
- Inside of homer-back IntelliJ config create 2 environment variables:
    - First one is `IMAGE_PATH` set to the path inside of nginx.conf. 
    - Second one is `influx.token` set to your influxdb token
- You can add command line arguments:
    - `--test-size=<number>` - for generating a number of random devices
    - `--device-type=<EDeviceType>` - for generating specific devices
- Set the flags from the application properties for mail sending if needed
- Change the admin email from application properties to receive superadmin password
- Start the homer-back application
- Inside of homer-front run `npm install` and then `npm start` to run the react application
- Run homer-simulation application
- You're all set. Basic users are as follows:
    - User `user@gmail.com` with password `12345678`
    - User `shared@gmail.com` with password `12345678`
    - Admin `admin@gmail.com` with password `12345678`
