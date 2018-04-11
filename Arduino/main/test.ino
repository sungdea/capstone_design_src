/*#include <CurieBLE.h>
#include <SoftwareSerial.h>
#include "rfid.h"

rfid module;
//const int tagLen = 16;  // RFID tag full length
//const int idLen = 13;   // RFID tag ID length
//const int RX=2, TX=3, dataNum=5;
unsigned char failConvert[idLen] = "XXXXXXXXXXXX";
unsigned char invalidData[idLen] = "NNNNNNNNNNNN";



//int data[dataNum]={0}, checksum=0;

SoftwareSerial rSerial(RX,TX); // RX, TX

BLEService RFIDSvc("03B80E5A-EDE8-4B33-A751-6CE34EC4C700"); // create service
// create switch characteristic and allow remote device to read
BLECharacteristic IDChar("7772E5DB-3868-4112-A1A9-F2669D106BF3", BLENotify | BLERead,idLen);

//void readTag();
void BLESetup();
void DeviceConnectHandler(BLEDevice central);
void DeviceDisconnectHandler(BLEDevice central);
void readTag();
/*boolean dataCheck();
boolean charToHex();
void printValue();
void clearValue();
*/

/*void setup() {
  Serial.begin(9600);
  rSerial.begin(9600);
 
  //BLESetup();
  
  // advertise the service
  //BLE.advertise();
  //Serial.println(("Bluetooth device active, waiting for connections..."));
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
      //IDChar.setValue(failConvert,idLen);
      Serial.println("don't convert chracter to hexdecimal.");
    }

    module.printValue();
    
    flag = module.dataCheck();
    if(!flag){
      //IDChar.setValue(invalidData,idLen) ;
      Serial.println("invalid rfid tag id.");
    }
    
    module.printValue();
    
    //IDChar.setValue((unsigned char*)id,idLen);

    module.clearValue();

    module.printValue();
  }
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

  //char *id = NULL;
  //id = module.getID();
  //IDChar.setValue((unsigned char*)id,idLen);
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
/*        if (readByte != 2 && readByte!= 13 && readByte != 10 && readByte != 3) {
        IDValue[i] = readByte;
        i++;
        }
    }
     module.setID(IDValue);
  }
 
}

boolean dataCheck(){
  int tmp = 0,i;
  for (i = 0; i < dataNum; i++) {
    tmp ^= data[i];
  }

  if (tmp == checksum) return true;
  else return false;
}

boolean charToHex() {
  char ch = 0;
  int tmp = 0, i;

  for (i = 0; i < idLen - 1; i++) {
    ch = IDValue[i];
    
    if (ch >= '0' && ch <= '9') {
      tmp |= ch - '0';
    }
    else if (ch >= 'A' && ch <= 'F') {
      tmp |= ch - 'A' + 10;
    }
    else {
      return false;
    }

    if (i % 2 == 0)  tmp <<= 4;
    else
    {
      if(i/2 != 5)  data[i / 2] = tmp;
      else  checksum = tmp;
      
      tmp = 0;
    }
  }
  return true;
}

void printValue() {
  Serial.print("id : ");
  Serial.println(IDValue);
  for (int i = 0; i < dataNum; i++){
    Serial.print("data ");
    Serial.print(i+1);
    Serial.print(" : ");
    Serial.println(data[i],HEX);
  }
  Serial.print("checkSum : ");
  Serial.println(checksum,HEX);
}

void clearValue(){
  char base_char[idLen] = { 0 };
  strncpy(IDValue, base_char, idLen);
  for (int i = 0; i<dataNum; i++) data[i] = 0;
  checksum = 0;
}*/

