#ifndef BASELED_ESP_NOW_H
#define BASELED_ESP_NOW_H

#include "globals.h"
#include <Arduino.h>
#include <espnow.h>

extern const int CONFIG;
extern const int ACTION;

extern const char *actionTypeStrs[2];

extern const int FIND_MASTER;
extern const int I_AM_MASTER;
extern const char *configPayloadStrs[2];

typedef struct Message {
  int actionType;
  int action;
  uint32_t payload;
} Message;

extern Message receivedMsg;
extern Message sentMsg;

// Broadcast a message to every device in range
extern uint8_t broadcastAddress[];

void OnMessageSent(uint8_t *mac, uint8_t status);

void OnMessageReceived(uint8_t *mac, uint8_t *incomingData, uint8_t len);

void espNowSend(uint8_t *mac, int actionType, int action, uint32_t payload);

void broadcast(int actionType, int action, uint32_t payload);

void setupAsMaster();

void setupAsSlave();

void initEspNow();

extern char macStr[18];

void SerialPrintMessageWithMac(uint8_t *mac, Message &message);

void SerialPrintSentMessage(uint8_t *mac, uint8_t status, Message &message);

void SerialPrintReceivedMessage(uint8_t *mac, Message &message);


#endif