/*********
  Rui Santos
  Complete project details at https://RandomNerdTutorials.com/esp-now-one-to-many-esp32-esp8266/
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files.
  
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
*********/

#include <ESP8266WiFi.h>
#include <espnow.h>

// REPLACE WITH YOUR ESP RECEIVER'S MAC ADDRESS
//uint8_t peer1[] = {0xBC, 0xFF, 0x4D, 0xCF, 0xA1, 0xCF};
uint8_t counter= 0;
uint8_t peers[5][6] = {
  {0xBC, 0xFF, 0x4D, 0xCF, 0xA1, 0xCF},
  {0xBC, 0xFF, 0x4D, 0xCF, 0xA1, 0xCF},
  {0xBC, 0xFF, 0x4D, 0xCF, 0xA1, 0xCF},
  {0xBC, 0xFF, 0x4D, 0xCF, 0xA1, 0xCF},
  {0xBC, 0xFF, 0x4D, 0xCF, 0xA1, 0xCF},

  };

char macStr[18];

// Broadcast a message to every device in range
uint8_t broadcastAddress[] = {0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};

typedef struct test_struct {
  int x;
  int y;
} test_struct;

test_struct test;
//esp_now_peer_info_t peerInfo;

// callback when data is sent
void OnDataSent(uint8_t *mac_addr, uint8_t status) {
  Serial.print("Packet to: ");
  // Copies the sender mac address to a string
  snprintf(macStr, sizeof(macStr), "%02x:%02x:%02x:%02x:%02x:%02x",
           mac_addr[0], mac_addr[1], mac_addr[2], mac_addr[3], mac_addr[4], mac_addr[5]);
  Serial.print(macStr);
  Serial.print(" send status:\t");
  Serial.println(status);
}
 
void formatMacAddress(const uint8_t *macAddr, char *buffer) {
  snprintf(buffer, sizeof(buffer), "%02x:%02x:%02x:%02x:%02x:%02x", macAddr[0], macAddr[1], macAddr[2], macAddr[3], macAddr[4], macAddr[5]);
}

void broadcast(const test_struct &data)
// Emulates a broadcast
{


  if(esp_now_is_peer_exist(broadcastAddress)!= 0){
    if (esp_now_add_peer(broadcastAddress, ESP_NOW_ROLE_SLAVE, 1, NULL, 0) != 0){
      Serial.println("Failed to add broadcast peer");
      return;
    }
  }

  // Send message    
  int result = esp_now_send(broadcastAddress, (uint8_t *) &data, sizeof(data)); 
  // Print results to serial monitor
  if (result == 0) {
      Serial.print("Boardcast with success to");
  }
    else {
      Serial.print("Error sending broadcast");
  }
}
 
void setup() {
  Serial.begin(115200);
 
  WiFi.mode(WIFI_STA);
 
  if (esp_now_init() != 0) {
    Serial.println("Error initializing ESP-NOW");
    return;
  }

  esp_now_set_self_role(ESP_NOW_ROLE_COMBO);
  esp_now_register_send_cb(OnDataSent);
   
  /* register peer
  peerInfo.channel = 0;  
  peerInfo.encrypt = false;*/
  // register first peer  
  //memcpy(peerInfo.peer_addr, peer1, 6);
  if (esp_now_add_peer(peers[0], ESP_NOW_ROLE_SLAVE, 1, NULL, 0) != 0){
    Serial.println("Failed to add peer");
    return;
  }
  /* register second peer  
  memcpy(peerInfo.peer_addr, broadcastAddress2, 6);
  if (esp_now_add_peer(peer1, ESP_NOW_ROLE_SLAVE, 1, NULL, 0) != ESP_OK){
    Serial.println("Failed to add peer");
    return;
  }*/

}
 
void loop() {
  test.x = random(0,20);
  test.y = random(0,20);
  // for(int i=0;i < counter; i++){
  //   uint8_t* peer= peers[i];
  //   int result = esp_now_send(peer, (uint8_t *) &test, sizeof(test));
    
  //   if (result == 0) {
  //     Serial.print("Sent with success to");
  //     formatMacAddress(peer, macStr);
  //     Serial.println(macStr);
  //   }
  //   else {
  //     Serial.print("Error sending the data to ");
  //     formatMacAddress(peer, macStr);
  //     Serial.println(macStr);
  //     Serial.println(result);
  //   }
  // }
  broadcast(test);
  delay(2000);
}
