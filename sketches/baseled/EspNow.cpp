#include "EspNow.h"

const int CONFIG = 0;
const int ACTION = 1;

const char *actionTypeStrs[2] = {
  "CONFIG",
  "ACTION"
};

const int FIND_MASTER = 0;
const int I_AM_MASTER = 1;
const char *configPayloadStrs[2] = {
  "FIND_MASTER",
  "I_AM_MASTER"
};

Message receivedMsg;
Message sentMsg;

// Broadcast a message to every device in range
uint8_t broadcastAddress[] = { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };

void OnMessageSent(uint8_t *mac, uint8_t status) {
#if DEBUG
  SerialPrintSentMessage(mac, status, sentMsg);
#endif
}

void OnMessageReceived(uint8_t *mac, uint8_t *incomingData, uint8_t len) {
  memcpy(&receivedMsg, incomingData, sizeof(receivedMsg));

#if DEBUG
  SerialPrintReceivedMessage(mac, receivedMsg);
#endif

  if (receivedMsg.actionType == ACTION) {
    currentAction = receivedMsg.action;
    currentColor = receivedMsg.payload;
    actionChanged = true;
  } else if (receivedMsg.actionType == CONFIG) {
    if (receivedMsg.action == I_AM_MASTER) {
      receivedMaster = true;
    } else if (receivedMsg.action == FIND_MASTER) {
      // If this node is the MASTER answer and send currentAction
      if (iAmMaster) {
        broadcast(CONFIG, I_AM_MASTER, 0);
        broadcast(ACTION, currentAction, currentColor);
      }
    }
  }
}

void espNowSend(uint8_t *mac, int actionType, int action, uint32_t payload) {
  sentMsg.actionType = actionType;
  sentMsg.action = action;
  sentMsg.payload = payload;

  if (esp_now_is_peer_exist(mac) != 0) {
    if (esp_now_add_peer(mac, ESP_NOW_ROLE_SLAVE, 1, NULL, 0) != 0) {
      Serial.println("Failed to add broadcast peer");
      return;
    }
  }

  // Send message
  int result = esp_now_send(mac, (uint8_t *)&sentMsg, sizeof(sentMsg));
  // Print results to serial monitor
  // if (result == 0) {
  //     Serial.println("Broadcast with success");
  // }
  //   else {
  //     Serial.println("Error sending broadcast");
  // }
}

void broadcast(int actionType, int action, uint32_t payload) {
  espNowSend(broadcastAddress, actionType, action, payload);
}

void setupAsSlave() {
  #if DEBUG
  Serial.println("Received MASTER, init me as SLAVE");
  #endif
  //esp_now_set_self_role(ESP_NOW_ROLE_SLAVE);
}

void setupAsMaster() {
#if DEBUG
  Serial.println("Did not receive MASTER, init me as MASTER");
#endif

  broadcast(CONFIG, I_AM_MASTER, 0);
  iAmMaster = true;
}

void initEspNow() {

  if (esp_now_init() != 0) {
#if DEBUG
    Serial.println("Error initializing ESP-NOW");
#endif
    return;
  }
  esp_now_set_self_role(ESP_NOW_ROLE_COMBO);
  esp_now_register_send_cb(OnMessageSent);
  esp_now_register_recv_cb(OnMessageReceived);

#if DEBUG
  Serial.println("Send Broadcast to find MASTER");
#endif
  broadcast(CONFIG, FIND_MASTER, 0);
  delay(3000);
  if (!receivedMaster) {
    setupAsMaster();
  } else {
    setupAsSlave();
  }
}


char macStr[18];

void formatMacAddress(uint8_t *macAddr) {
  snprintf(macStr, sizeof(macStr), "%02x:%02x:%02x:%02x:%02x:%02x", macAddr[0], macAddr[1], macAddr[2], macAddr[3], macAddr[4], macAddr[5]);
}

void SerialPrintMessageWithMac(uint8_t *mac, Message &message) {
  formatMacAddress(mac);
  Serial.print("MAC: ");
  Serial.println(macStr);
  Serial.print("actionType: ");
  Serial.println(actionTypeStrs[message.actionType]);
  Serial.print("action: ");
  if (message.actionType == CONFIG) {
    Serial.println(configPayloadStrs[message.action]);
  } else {
    Serial.println(message.action);
    Serial.print("payload: ");
    Serial.println(String(message.payload, 16));
  }
}

void SerialPrintSentMessage(uint8_t *mac, uint8_t status, Message &message) {
  Serial.println("------- Sent -------");
  SerialPrintMessageWithMac(mac, message);
  Serial.print("status: ");
  Serial.println(status);
  Serial.println("----------------");
}

void SerialPrintReceivedMessage(uint8_t *mac, Message &message) {
  Serial.println("------- Received -------");
  SerialPrintMessageWithMac(mac, message);
  Serial.println("----------------");
}