# Bottle-LED
ATTINY-85 driven RGB LED for Bottle Lamps

# Hardware
<img src="./doc/schematic.PNG" width="600">

* Digispark ATTINY-85 Board
* WS2812b single LED Strip (+ 1kOhm Resistor for Data Pin)
* Tactile Button (+ 10kOhm Pulldown Resistor)
* TP4056 Li-Ion Battery Charger
* Li-Ion Battery (3.7v, 600mAh)

# Code
* Toggle through Colors and Animations by pressing Button
* FastLED Library to control WS2812b LED
* EEPROM to permanently store Color set by User

# 3D Case
<img src="./doc/3dcase.PNG" width="600">
<img src="./doc/3dcase_open.PNG" width="600">