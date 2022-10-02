/*********
  Rui Santos
  Complete project details at https://RandomNerdTutorials.com/esp-now-one-to-many-esp32-esp8266/
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files.
  
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
*********/
#include "globals.h"
#include "WebSocket.h"
#include "EspNow.h"
#include "RGBControl.h"
#include <ESP8266WiFi.h>

#include <ArduinoOTA.h>

#define OTA_PASSWORD "esp8266ota"

void initWifi() {
  // Set Wifi mode to be able to use local WiFi Connection + ESPNow
  WiFi.mode(WIFI_AP_STA);
  // Connect to Wifi to expose REST API
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
#if DEBUG
    Serial.println("Connecting to local WiFi...");
#endif
  }
#if DEBUG
  Serial.print("My MAC Addr: ");
  Serial.println(WiFi.macAddress());
  Serial.print("Got IP Address: ");
  Serial.println(WiFi.localIP());
  Serial.print("WiFi Channel: ");
  Serial.println(WiFi.channel());
#endif
}

void setup() {
  initRGB();
  loadColorAndAction();
  updateRGB(currentAction, currentColor);
#if DEBUG
  Serial.begin(57600);
#endif
  initWifi();
  initEspNow();
  if (iAmMaster) {
    setupWebSocket();
  }
  ArduinoOTA.setPassword(OTA_PASSWORD);
  ArduinoOTA.begin();
}

void loop() {
  if (iAmMaster) {
    webSocket.loop();
    if (actionChanged) {
      broadcast(ACTION, currentAction, currentColor);
      saveColorAndAction();
    }
    delay(10);
  }
  if (actionChanged) {
    updateRGB(currentAction, currentColor);
    actionChanged = false;
  }
  ArduinoOTA.handle();
}