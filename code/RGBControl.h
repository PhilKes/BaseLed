#ifndef BASELED_RGBCONTROL_H
#define BASELED_RGBCONTROL_H

#include <Arduino.h>

void initRGB();
void showRGB(uint32_t color);
void updateLed();

#define ANIM_RGB 0
#define ANIM_RED 1
#define ANIM_GREEN 2
#define ANIM_BLUE 3
#define ANIM_RGB_WHEEL 4

#endif