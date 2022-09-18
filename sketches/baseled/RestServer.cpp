#include <cstdlib>
#include "RestServer.h"

ESP8266WebServer restServer(HTTP_REST_PORT);

void onPost() {
#if DEBUG
  Serial.print("POST / called");
#endif
  for (int i = 0; i < restServer.args(); i++) {
    if (restServer.argName(i) == "action") {
      currentAction = restServer.arg(i).toInt();
      actionChanged = true;
#if DEBUG
      Serial.print("Changing currentAction to: ");
      Serial.println(currentAction);
#endif
    } else if (restServer.argName(i) == "rgb") {
      // Load RGB value from query param
      currentColor = strtol(restServer.arg(i).c_str(), NULL, 16);
#if DEBUG
      Serial.print("Changing currentColor to: ");
      Serial.println(String(currentColor, 16));
#endif
    }
  }
  restServer.send(200, "text/json", "");
}

void onGetAction() {
#if DEBUG
  Serial.print("POST / called");
#endif
  restServer.send(200, "text/json", "{ \"currentAction\": " + String(currentAction) + ", \"currentColor\": 0x" + String(currentColor, 16) + "}");
}

void setupRestServer() {
  restServer.on(F("/"), HTTP_POST, onPost);
  restServer.on(F("/action"), HTTP_GET, onGetAction);
  restServer.begin();
}