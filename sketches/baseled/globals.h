#ifndef BASELED_GLOBALS_H
#define BASELED_GLOBALS_H

#include <Arduino.h>
#define DEBUG 1

extern bool actionChanged;
extern int currentAction;
extern uint32_t currentColor;

extern bool receivedMaster;
extern bool iAmMaster;

extern const char *ssid;
extern const char *password;

#endif
