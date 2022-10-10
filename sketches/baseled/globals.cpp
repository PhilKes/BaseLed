#include "globals.h"
#include "RGBControl.h"
#include <EEPROM.h>

bool actionChanged = true;
uint8_t currentAction = 0;
uint32_t currentColor = 0xFFFFFF;
uint8_t currentBrightness = 0xFF;

uint16_t currentFrame = 0;

bool receivedMaster = false;
bool iAmMaster = false;

// Replace with your network credentials (STATION)
const char *ssid = "Phils-Toaster";
const char *password = "Pi1ad,bhia";

#define EEPROM_SIZE 12
#define ADDRESS_ACTION 0
#define ADDRESS_COLOR 2
#define ADDRESS_BRIGHTNESS 6

void loadColorAndAction() {
  EEPROM.begin(EEPROM_SIZE);
  EEPROM.get(ADDRESS_ACTION, currentAction);
  EEPROM.get(ADDRESS_COLOR, currentColor);
  EEPROM.get(ADDRESS_BRIGHTNESS, currentBrightness);
  EEPROM.end();

  if (currentAction > ANIM_RGB_WHEEL) {
    currentAction = 0;
    currentColor = 0xFFFFFF;
    currentBrightness = 255;
  }
#if DEBUG
  Serial.printf("Loaded from EEPROM: action: %d color: %x brightness: %d\n", currentAction, currentColor, currentBrightness);
#endif
}

void saveColorAndAction() {
  EEPROM.begin(EEPROM_SIZE);
  EEPROM.put(ADDRESS_ACTION, currentAction);
  EEPROM.put(ADDRESS_COLOR, currentColor);
  EEPROM.put(ADDRESS_BRIGHTNESS, currentBrightness);
  EEPROM.commit();
  EEPROM.end();
#if DEBUG
  Serial.printf("Stored to EEPROM: action: %d color: %x brightness: %d\n", currentAction, currentColor, currentBrightness);
#endif
}