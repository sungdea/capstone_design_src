#include <SoftwareSerial.h>
#include <CurieBLE.h>

BLEService tagService("19B10000-E8F2-537E-4F6C-D104768A1214"); 
BLEUnsignedCharCharacteristic tagTransferCharacteristic("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify);
BLEDevice central;
SoftwareSerial rSerial(2,3) // RX, TX

const int tagLen = 16;  // RFID tag full length
const int idLen = 13;   // RFID tag ID length

char tagId[idLen];

void readTag();
void BLEsetup();
boolean BLEconnect();
void updateTagId();

void setup() {
  Serial.begin(9600);
  rSerial.begin(9600);

  BLEsetup();
}

void loop() {

  if(!BLEconnect()) return;

  readTag();

  if(strlen(newtag) == 0) return; // If there is no data to read, return
  else{
    updateTagId();
  }
  
  for (int i=0; i < idLen; i++) {
    newTag[i] = 0;
  }
}

void readTag(){
  int i=0;
  int readByte;
  boolean tag = false;
  
  if(rSerial.available() == tagLen) tag = true; // RFID tag가 맞는지 확인
  
  if(tag==true){
    while(rSerial.available()){
      readByte = rSerial.read(); // tag 1Byte씩 read

        /* first one byte and last three byte skip.
           first one byte(0x02) is STX/start of text, last three byte is CR/carrige return(0x13),
           LF/linefeed(0x10), ETX/end of text. */
        if (readByte != 2 && readByte!= 13 && readByte != 10 && readByte != 3) {
        newTag[i] = readByte;
        i++;
        }

        if(readByte == 3) tag=false;
    }
  }
}

void BLEsetup(){ 
  BLE.begin();
  BLE.setLocalName("RFID Tag");
  BLE.setAdvertisedService(tagService);

  tagService.addCharacteristic(tagTransferCharacteristic);
  BLE.addService(tagService);

  tagTransferCharacteristic.setValue(0);
  BLE.advertise();
}

boolean BLEconnect(){
  while(true){
  central = BLE.central();
  if(central.connected()) break;  
  };
}

void updateTagId(){
  tagTransferCharacteristic.setValue(newTag,idLen);
}
