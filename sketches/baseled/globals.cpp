#include "globals.h"
#include <EEPROM.h>

bool actionChanged = true;
uint8_t currentAction = 0;
uint32_t currentColor = 0xFFFFFF;

bool receivedMaster = false;
bool iAmMaster = false;

// Replace with your network credentials (STATION)
const char *ssid = "Phils-Toaster";
const char *password = "Pi1ad,bhia";

#define EEPROM_SIZE 12
#define ADDRESS_ACTION 0
#define ADDRESS_COLOR 2

void loadColorAndAction() {
  EEPROM.begin(EEPROM_SIZE);
  EEPROM.get(ADDRESS_ACTION, currentAction);
  EEPROM.get(ADDRESS_COLOR, currentColor);
  EEPROM.end();

  if (currentAction > 3) {
    currentAction = 0;
    currentColor = 0xFFFFFF;
  }
#if DEBUG
  Serial.print("Loaded from EEPROM: action: ");
  Serial.print(currentAction);
  Serial.print(" color:");
  Serial.println(String(currentColor, 16));
#endif
}

void saveColorAndAction() {
  EEPROM.begin(EEPROM_SIZE);
  EEPROM.put(ADDRESS_ACTION, currentAction);
  EEPROM.put(ADDRESS_COLOR, currentColor);
  EEPROM.commit();

#if DEBUG
  Serial.print("Stored to EEPROM: action: ");
  Serial.print(currentAction);
  Serial.print(" color:");
  Serial.println(String(currentColor, 16));
#endif
}