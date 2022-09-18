#ifndef REST_SERVER_H
#define REST_SERVER_H

#include "globals.h"
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

#define HTTP_REST_PORT 8080

extern ESP8266WebServer restServer;

void onPost();

void onGetAction();

void setupRestServer();


#endif