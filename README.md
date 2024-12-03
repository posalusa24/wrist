# WRIST

Wi-Fi RSSI-based Indoor Spatial Tracking (Client Android App and Server)

Presentation: https://docs.google.com/presentation/d/1cEOPx2ekwIWLqIIvTPXMQkT57gC0DKxDI4PjYS2aXBQ/edit?usp=sharing

## How it works

* Given that there is a Wi-Fi AP for each room in a building, we can determine which room a mobile client is in by checking which Wi-Fi AP is currently closest to it.
* The mobile client periodically scans for available Wi-Fi APs and notes the SSID of one with the highest RSSI (Received Signal Strength Indicator).
This SSID along with the mobile client’s ID is then sent to a server.
* The server has a mapping of SSIDs to rooms which is used to determine which room the client is in.
* This location information can then be retrieved from the server for monitoring purposes.

## Components

* Android App Client
  * Runs on a user’s phone and sends Wi-Fi RSSI information to the server.
* Location Mapping Server
  * A Python program.
  * Receives RSSI information from clients and determines their locations according to a mapping.
  * Administrators can monitor the locations of users here.
* Public MQTT Server
  * Passes messages between the Android App Client and Location Mapping Server.
