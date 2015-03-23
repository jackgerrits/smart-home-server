# smart-home-server

Web/application server for interfacing with client and connected Phidget.

###Dependencies
* Phidget Java library
* JSON.simple Java Library

###Installation
1. Compile with libraries
2. Put client in www directory at root of repo
3. Modify options.prop with correct information
4. Run

Ensure that correct sensors are in the corresponding places in _options.prop_, since they are analog there is no way to tell if there is no sensor connected or if the value is just 0.

###Web API
* */* - Serves static pages in www directory
* */push* - Long polling hook for server pushing events to client
* */data/sensors* - Returns list of names of all connected sensors
* */data/sensors/[sensorName]* - Returns current value of sensor
