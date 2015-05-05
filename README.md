# smart-home-server

Web/application server for interfacing with client and connected Phidgets.

###Dependencies
* [Phidget Java library](http://www.phidgets.com/docs/Language_-_Java)
* [JSON.simple Java Library](https://code.google.com/p/json-simple/)

###Installation
1. Compile with dependencies in classpath
2. Generate keystore for SSL using
```
keytool -genkey -alias _alias_ -keyalg RSA -keystore _keystore.jks_ -keysize 2048
```
3. Put client in www directory at root of repository
4. Modify __options.prop__ with correct information
5. Modify __events.json__ with custom rule definitions
5. Run

options.prop, events.json and Main.java provide usage examples.

Ensure that correct sensors are in the corresponding places in _options.prop_, since they are analog there is no way to tell if there is no sensor connected or if the value is just 0.

###Web API
* */* - Serves static pages in www directory

Below requests require use of POST method, with a JSON object for authentication.
```
{
    //These values are dependant on server configuration
    "username":"<username here>",
    "username":"<password here>"
}
```

* */data/feed* - Long polling hook for server pushing events to client
* */data/sensors* - Returns list of names of all connected sensors
* */data/sensors/[sensorName]* - Returns current value of sensor
