#include "globals.h"

bool actionChanged = true;
int currentAction = 0;
uint32_t currentColor = 0xFFFFFF;

bool receivedMaster = false;
bool iAmMaster = false;

// Replace with your network credentials (STATION)
const char *ssid = "Phils-Toaster";
const char *password = "Pi1ad,bhia";