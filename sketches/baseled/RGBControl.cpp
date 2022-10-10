#include "RGBControl.h"
#include "globals.h"
#include <Adafruit_NeoPixel.h>

#define PIN D2
#define NUMPIXELS 1

#define MAX_HUE 65535
#define RGB_WHEEL_FRAMETIME 20
#define RGB_WHEEL_FRAME_STEP 10

Adafruit_NeoPixel pixels = Adafruit_NeoPixel(1, PIN, NEO_GRB + NEO_KHZ800);
uint16_t maxFrame = MAX_HUE;

void initRGB() {
  pixels.begin();
}

void showRGB(uint32_t color) {
  pixels.setPixelColor(0, color);
  pixels.show();
}

void updateLed() {
  switch (currentAction) {
    case ANIM_RGB:
      showRGB(currentColor);
      break;
    case ANIM_RED:
      showRGB(0xFF0000);
      break;
    case ANIM_GREEN:
      showRGB(0x0000FF);
      break;
    case ANIM_BLUE:
      showRGB(0x00FF00);
      break;
    case ANIM_RGB_WHEEL:
      showRGB(pixels.ColorHSV(currentFrame, 255, 255));
      currentFrame = (currentFrame + RGB_WHEEL_FRAME_STEP) % maxFrame;
      delay(RGB_WHEEL_FRAMETIME);
      break;
    default:
      showRGB(0xFFFFFF);
      break;
  }
}