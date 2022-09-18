#ifndef BASELED_RGBCONTROL_H
#define BASELED_RGBCONTROL_H

#include <Arduino.h>

void initRGB();
void showRGB(uint32_t color);
void updateRGB(int currentAction, uint32_t color);


#endif