#include "RGBControl.h"
#include <Adafruit_NeoPixel.h>

#define PIN D2
// How many NeoPixels are attached to the Arduino?
#define NUMPIXELS 1

Adafruit_NeoPixel pixels = Adafruit_NeoPixel(1, PIN, NEO_GRB + NEO_KHZ800);

void initRGB() {
  pixels.begin();
}

void showRGB(uint32_t color) {
  pixels.setPixelColor(0, color);
  pixels.show();
}

void updateRGB(int action,uint32_t color) {
  switch (action) {
    case 0:
      showRGB(0x00FF00);
      break;
    case 1:
      showRGB(0xFF0000);
      break;
    case 2:
      showRGB(0x0000FF);
      break;
    case 3:
      showRGB(color);
      break;
    default:
      showRGB(0xFFFFFF);
      break;
  }
}