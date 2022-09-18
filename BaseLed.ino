#include <Adafruit_NeoPixel.h>
#include "EEPROM.h"

#define LED_PIN     PB4
#define BTN_PIN     PB3

#define NUM_LEDS    1
#define BRIGHTNESS 20

#define MAX_MODE    9 // Highest mode number + 1
#define RGB_WHEEL_MODE 0
#define RGB_SAVED_MODE 1

#define RGB_WHEEL_FRAMETIME 1
#define RGB_WHEEL_FRAME_STEP 10

#define BUTTON_HIGH_COUNTER_THRESHOLD 20

#define MAX_HUE 65535

//CRGB leds[NUM_LEDS];

Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUM_LEDS, LED_PIN, NEO_GRB + NEO_KHZ800);

int buttonState = 0;         // variable for reading the pushbutton status
int buttonStateBefore = 0;
 
int mode = RGB_WHEEL_MODE;
uint16_t frame = 0;
uint16_t maxFrame = MAX_HUE;

boolean animationStopped = false;

int buttonHighCounter = 0;

char buff[50];

void setup() 
{
  pinMode(BTN_PIN, INPUT);
  //FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUM_LEDS);
  //FastLED.setBrightness(BRIGHTNESS);
 // Serial.begin(9600);
  pixels.begin();
  pixels.setBrightness(255); // set accordingly
  pixels.show(); // Initialize all pixels to 'off'
}


void loop() 
{

  buttonState = digitalRead(BTN_PIN);
  if(buttonState == HIGH){
     buttonHighCounter++;
   // sprintf(buff, "ButtonCounter %d", buttonHighCounter);
    //Serial.println(buff);
  }
  // check if the button was pressed before and was now released
  if (buttonStateBefore == HIGH && buttonState == LOW) 
  {
      // Stop current animation if Button is pressed for at least 3 Seconds
      if(buttonHighCounter >= BUTTON_HIGH_COUNTER_THRESHOLD)
      {
        // If RGB Wheel Mode is active and animation has been stopped
        // Pressing the button for ~3 Seconds saves the current Color permanently for RGB_SAVED_MODEESET_MODE
        if(mode == RGB_WHEEL_MODE && animationStopped)
        {
          uint32_t currentColor = pixels.getPixelColor(0);
          uint8_t r = currentColor >> 16;
          uint8_t g = currentColor >> 8;
          uint8_t b = currentColor;
          //sprintf(buff, "Writing to EEPROM R:%d G:%d B:%d", r, g, b);
         // Serial.println(buff);
          // Save current Color to EEPROM 
           EEPROM.write(1, r);
           EEPROM.write(2, g);
           EEPROM.write(3, b);
        }
      }
      else
      {
        // If RGB Wheel Mode is active only stop the Wheel, do not change mode
        if(mode == RGB_WHEEL_MODE && !animationStopped)
        {
         // Serial.println("animation Stopped");
          animationStopped = true;  
        }
        else
        {
          mode = (mode+1) % MAX_MODE;
          animationStopped = false;
          frame=0;
          switch(mode)
          {
            // Load Frame values for animations
            case RGB_WHEEL_MODE:
              maxFrame=MAX_HUE;
              break;  
          }
        }
      }
      buttonHighCounter=0;
  }
  switch(mode)
  {

    case RGB_WHEEL_MODE:
      // Color Wheel
      if(animationStopped)
        break;
     // leds[0] = getRGBColorFromHSV(frame,1.0,1.0);
      pixels.setPixelColor(0, pixels.ColorHSV(frame,255,255));
      frame = (frame+RGB_WHEEL_FRAME_STEP) % maxFrame;
      delay(RGB_WHEEL_FRAMETIME); 
      break;
   case RGB_SAVED_MODE:
      if(frame == 0){
        // Loading saved Color to EEPROM 
          pixels.setPixelColor(0, pixels.Color(EEPROM.read(1),EEPROM.read(2),EEPROM.read(3)));
          frame = 1;
      }
      break;
    case 2:
      // RED
      //leds[0] = CRGB(255, 0, 0);
      pixels.setPixelColor(0, pixels.Color(255, 0, 0));
      break; 
    case 3:
      // GREEN
      pixels.setPixelColor(0, pixels.Color(0, 255, 0));
      break; 
    case 4:
      // BLUE
      pixels.setPixelColor(0, pixels.Color(0, 0, 255));
      break; 
   case 5:
      // YELLOW
      pixels.setPixelColor(0, pixels.Color(255, 255, 0));
      break; 
   case 6:
      // VIOLET
      pixels.setPixelColor(0, pixels.Color(255, 0, 255));
      break; 
   case 7:
      // CYAN
      pixels.setPixelColor(0, pixels.Color(0, 255, 255));
      break; 
   case 8:
      // WHITE
      pixels.setPixelColor(0, pixels.Color(255, 255, 255));
      break; 

  }
 // FastLED.show();
  pixels.show();
  buttonStateBefore = buttonState;
  delay(10); 
}

//Convert a given HSV (Hue Saturation Value) to RGB(Red Green Blue)
//  h is hue value, integer between 0 and 360
//  s is saturation value, double between 0 and 1
//  v is value, double between 0 and 1
//http://splinter.com.au/blog/?p=29
/*
uint32_t getRGBColorFromHSV(int h, double s, double v) 
{
  //this is the algorithm to convert from RGB to HSV
  double r=0; 
  double g=0; 
  double b=0;

  double hf=h/60.0;

  int i=(int)floor(h/60.0);
  double f = h/60.0 - i;
  double pv = v * (1 - s);
  double qv = v * (1 - s*f);
  double tv = v * (1 - s * (1 - f));

  switch (i)
  {
    case 0: //rojo dominante
      r = v;
      g = tv;
      b = pv;
      break;
    case 1: //verde
      r = qv;
      g = v;
      b = pv;
      break;
    case 2: 
      r = pv;
      g = v;
      b = tv;
      break;
    case 3: //azul
      r = pv;
      g = qv;
      b = v;
      break;
    case 4:
      r = tv;
      g = pv;
      b = v;
      break;
    case 5: //rojo
      r = v;
      g = pv;
      b = qv;
      break;
  }

  //set each component to a integer value between 0 and 255
  int red=constrain((int)255*r,0,255);
  int green=constrain((int)255*g,0,255);
  int blue=constrain((int)255*b,0,255);
  //sprintf(buff, "%d %d %d", red, green, blue);
 // Serial.println(buff);
  
  return pixels.Color(red,green,blue);
}*/
