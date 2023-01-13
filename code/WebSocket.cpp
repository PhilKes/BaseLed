#include <cstdlib>
#include "WebSocket.h"
#include "RGBControl.h"

WebSocketsServer webSocket(WEB_SOCKET_PORT);

// void onPost() {
// #if DEBUG
//   Serial.print("POST / called");
// #endif
//   for (int i = 0; i < restServer.args(); i++) {
//     if (restServer.argName(i) == "action") {
//       currentAction = restServer.arg(i).toInt();
//       actionChanged = true;
// #if DEBUG
//       Serial.print("Changing currentAction to: ");
//       Serial.println(currentAction);
// #endif
//     } else if (restServer.argName(i) == "rgb") {
//       // Load RGB value from query param
//       currentColor = strtol(restServer.arg(i).c_str(), NULL, 16);
// #if DEBUG
//       Serial.print("Changing currentColor to: ");
//       Serial.println(String(currentColor, 16));
// #endif
//     }
//   }
//   restServer.send(200, "text/json", "");
// }

// void onGetAction() {
// #if DEBUG
//   Serial.print("GET / called");
// #endif
//   restServer.send(200, "text/json", "{ \"currentAction\": " + String(currentAction) + ", \"currentColor\": 0x" + String(currentColor, 16) + "}");
// }

// void onHead() {
// #if DEBUG
//   Serial.print("HEAD / called");
// #endif
//   rest
//   Server.send(200, "text/plain", "");
// }

void onWebSocketEvent(uint8_t num, WStype_t type, uint8_t* payload, size_t length) {
  String cmd;
  switch (type) {
    case WStype_DISCONNECTED:
#if DEBUG
      Serial.printf("[%u] Disconnected!\n", num);
#endif
      break;

    case WStype_CONNECTED:
      {

        // send current action+color to client
        char str[64];
        sprintf(str, "%d-%06x-%02x", currentAction, currentColor, currentBrightness);
        IPAddress ip = webSocket.remoteIP(num);
#if DEBUG
        Serial.printf("[%u] Connected from %d.%d.%d.%d url: %s\n", num, ip[0], ip[1], ip[2], ip[3], payload);
        Serial.printf("Sending: %s", str);
#endif
        webSocket.sendTXT(num, str);
      }
      break;
    case WStype_TEXT:
//Serial.printf("[%u] get Text: %s\n", num, payload);
// Command format:
// {action}-{payload/color in hex}
// send message to client
#if DEBUG
      Serial.println("Message from Client:");
      Serial.printf("%s\n", payload);
#endif
      cmd = String((char*)payload);
      currentAction = cmd.charAt(0) - '0';
      currentColor = strtol(cmd.substring(2).substring(0, 6).c_str(), NULL, 16);
      currentBrightness = strtol(cmd.substring(2).substring(7, 9).c_str(), NULL, 16);

      actionChanged = true;
      saveColorAndAction();
      // char str[64];
      // sprintf(str, "Action:%d Color:0x%x", currentAction, currentColor);
      // webSocket.sendTXT(num, str);
      // send data to all connected clients
      // webSocket.broadcastTXT("message here");
      break;
    case WStype_BIN:
#if DEBUG
      Serial.printf("[%u] get binary length: %u\n", num, length);
#endif
      hexdump(payload, length);
      // send message to client
      // webSocket.sendBIN(num, payload, length);
      break;
  }
}

void setupWebSocket() {
  webSocket.begin();
  webSocket.onEvent(onWebSocketEvent);
#if DEBUG
  Serial.println("Setup WebSocket");
#endif
}