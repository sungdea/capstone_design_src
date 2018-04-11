#include <CurieBLE.h>
#include <SoftwareSerial.h>
#include "rfid.h"

rfid module;
unsigned char failConvert[idLen] = "XXXXXXXXXXXX";
unsigned char invalidData[idLen] = "NNNNNNNNNNNN";

SoftwareSerial rSerial(RX,TX); // RX, TX

BLEService RFIDSvc("03B80E5A-EDE8-4B33-A751-6CE34EC4C700"); // create service
// create switch characteristic and allow remote device to read
BLECharacteristic IDChar("7772E5DB-3868-4112-A1A9-F2669D106BF3", BLENotify | BLERead,idLen);

void readTag();
void BLESetup();
void DeviceConnectHandler(BLEDevice central);
void DeviceDisconnectHandler(BLEDevice central);

void setup() {
  Serial.begin(9600);
  rSerial.begin(9600);
 
  BLESetup();
  
  // advertise the service
  BLE.advertise();
  Serial.println(("Bluetooth device active, waiting for connections..."));
}

void loop() {
  char *id = NULL;
  boolean flag = false;
  readTag();

  id = module.getID();
  
  if(strlen(id) == 0) return; // If there is no data to read, return
  else{
    module.printValue();
    flag = module.charToHex();
    if(!flag){
      IDChar.setValue(failConvert,idLen);
    }

    module.printValue();
    flag = module.dataCheck();
    if(!flag){
      IDChar.setValue(invalidData,idLen) ;
    }
    
    IDChar.setValue((unsigned char*)id,idLen);
  }
  
  module.clearValue();
}

void BLESetup()
{
  BLE.begin();
  // set the local name peripheral advertises
  BLE.setLocalName("RFID tag");

  // set the UUID for the service this peripheral advertises
  BLE.setAdvertisedServiceUuid(RFIDSvc.uuid());

  // add service and characteristic
  RFIDSvc.addCharacteristic(IDChar);
  BLE.addService(RFIDSvc);

  // assign event handlers for connected, disconnected to peripheral
  BLE.setEventHandler(BLEConnected, DeviceConnectHandler);
  BLE.setEventHandler(BLEDisconnected, DeviceDisconnectHandler);

  char *id = NULL;
  id = module.getID();
  IDChar.setValue((unsigned char*)id,idLen);
}

void DeviceConnectHandler(BLEDevice central) {
  // central connected event handler
  Serial.print("Connected event, central: ");
  Serial.println(central.address());
}

void DeviceDisconnectHandler(BLEDevice central) {
  // central disconnected event handler
  Serial.print("Disconnected event, central: ");
  Serial.println(central.address());
}

void readTag(){
  int i=0;
  int readByte;
  boolean tag = false;
  char IDValue[idLen] = { 0 };
  
  if(rSerial.available() == tagLen) tag = true; // RFID tag가 맞는지 확인
  
  if(tag==true){
    while(rSerial.available()){
      readByte = rSerial.read(); // tag 1Byte씩 read

        /* first one byte and last three byte skip.
           first one byte(0x02) is STX/start of text, last three byte is CR/carrige return(0x13),
           LF/linefeed(0x10), ETX/end of text. */
        if (readByte != 2 && readByte!= 13 && readByte != 10 && readByte != 3) {
        IDValue[i] = readByte;
        i++;
        }
    }
     module.setID(IDValue);
  }
 
}
