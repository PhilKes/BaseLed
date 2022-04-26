#include <FastLED.h>
#include <EEPROM.h>

#define LED_PIN     7
#define BTN_PIN     3

#define NUM_LEDS    1
#define BRIGHTNESS 30

#define MAX_MODE    9 // Highest mode number + 1
#define RGB_WHEEL_MODE 0
#define RGB_PRESET_MODE 1
#define RGB_WHEEL_FRAMETIME 100

#define BUTTON_HIGH_COUNTER_THRESHOLD 20


CRGB leds[NUM_LEDS];
int buttonState = 0;         // variable for reading the pushbutton status
int buttonStateBefore = 0;
 
int mode = 0;
int frame = 0;
int maxFrame = 360;

boolean animationStopped = false;

int buttonHighCounter = 0;

char buff[50];

void setup() 
{
  pinMode(BTN_PIN, INPUT);
  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUM_LEDS);
  FastLED.setBrightness(BRIGHTNESS);
  Serial.begin(9600);
}


void loop() 
{

  buttonState = digitalRead(BTN_PIN);
  if(buttonState == HIGH){
     buttonHighCounter++;
    sprintf(buff, "ButtonCounter %d", buttonHighCounter);
    Serial.println(buff);
  }
  // check if the button was pressed before and was now released
  if (buttonStateBefore == HIGH && buttonState == LOW) 
  {
      // Stop current animation if Button is pressed for at least 3 Seconds
      if(buttonHighCounter >= BUTTON_HIGH_COUNTER_THRESHOLD)
      {
        // If RGB Wheel Mode is active and animation has been stopped
        // Pressing the button for ~3 Seconds saves the current Color permanently for RGB_PRESET_MODE
        if(mode == RGB_WHEEL_MODE && animationStopped)
        {
          sprintf(buff, "Writing to EEPROM R:%d G:%d B:%d", leds[0].red, leds[0].green, leds[0].blue);
          Serial.println(buff);
          // Save current Color to EEPROM 
           EEPROM.write(1, leds[0].red);
           EEPROM.write(2, leds[0].green);
           EEPROM.write(3, leds[0].blue);
        }
      }
      else
      {

        // If RGB Wheel Mode is active only stop the Wheel
        if(mode == RGB_WHEEL_MODE && !animationStopped)
        {
          Serial.println("animation Stopped");
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
              maxFrame=360;
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
      leds[0] = getRGBColorFromHSV(frame,1.0,1.0);
      frame = (frame+1) % maxFrame;
      delay(RGB_WHEEL_FRAMETIME); 
      break;
   case RGB_PRESET_MODE:
      if(frame == 0){
        // Loading saved Color to EEPROM 
          leds[0]= CRGB(EEPROM.read(1),EEPROM.read(2),EEPROM.read(3));
          frame = 1;
      }
      break;
    case 2:
      // RED
      leds[0] = CRGB(255, 0, 0);
      break; 
    case 3:
      // GREEN
      leds[0] = CRGB(0, 255, 0);
      break; 
    case 4:
      // BLUE
      leds[0] = CRGB(0, 0, 255);
      break; 
   case 5:
      // YELLOW
      leds[0] = CRGB(255, 255, 0);
      break; 
   case 6:
      // VIOLET
      leds[0] = CRGB(255, 0, 255);
      break; 
   case 7:
      // CYAN
      leds[0] = CRGB(0, 255, 255);
      break; 
   case 8:
      // WHITE
      leds[0] = CRGB(255, 255, 255);
      break; 

  }
  FastLED.show();
  buttonStateBefore = buttonState;
  delay(10); 
}

//Convert a given HSV (Hue Saturation Value) to RGB(Red Green Blue)
//  h is hue value, integer between 0 and 360
//  s is saturation value, double between 0 and 1
//  v is value, double between 0 and 1
//http://splinter.com.au/blog/?p=29
CRGB getRGBColorFromHSV(int h, double s, double v) 
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
  
  return CRGB(red,green,blue);
}
