# smart-home-server

Web/application server for interfacing with client and connected Phidget.

###Dependencies
* Phidget Java library
* JSON.simple Java Library

###Installation
1. Compile with libraries
2. Generate keystore for SSL using
```
keytool -genkey -alias _alias_ -keyalg RSA -keystore _keystore.jks_ -keysize 2048
```
3. Put client in www directory at root of repository
4. Modify options.prop with correct information
5. Run

Ensure that correct sensors are in the corresponding places in _options.prop_, since they are analog there is no way to tell if there is no sensor connected or if the value is just 0.

###Web API
* */* - Serves static pages in www directory
* */data/feed* - Long polling hook for server pushing events to client
* */data/sensors* - Returns list of names of all connected sensors
* */data/sensors/[sensorName]* - Returns current value of sensor
