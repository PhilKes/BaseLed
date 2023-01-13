#ifndef REST_SERVER_H
#define REST_SERVER_H

#include "globals.h"
#include <ESP8266WiFi.h>
#include <WebSocketsServer.h>

#define WEB_SOCKET_PORT 81

extern WebSocketsServer webSocket;

// void onPost();

// void onGetAction();

void setupWebSocket();


#endif