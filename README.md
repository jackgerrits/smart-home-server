# smart-home-server

Web/application server for interfacing with client and connected Phidgets.

Demo: [youtu.be/v49lkMFm10s](youtu.be/v49lkMFm10s)

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

###Program flow
Server creates SensorController which creates Phidgets and loads events from events.json
* Phidgets attach listeners for sensor change events (Phidget API)
* These change events are sent to the SensorController and the EventTester tests each change event according to the rule definitions
* Detected events are pushed onto a queue, and also checked if they compose an AND event

When a client requests */data/feed*, responds with Event at the front of the queue, or holds the request until an event happens

When a client requests */data/sensors*, responds with a list of all the sensors as defined in options.prop

When a client requests */data/sensors/[sensorName]*, responds with the current value of that sensor if it exists, or 404 if it doesn't
