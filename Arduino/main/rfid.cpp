#include "rfid.h"

rfid::rfid(){
   char base_char[idLen] = {0};
   strncpy(IDValue, base_char, idLen);
   for(int i=0;i<dataNum;i++) data[i]=0;
   checksum = 0;
}

/*void rfid::readTag(SoftwareSerial rSerial){
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
        /*if (readByte != 2 && readByte!= 13 && readByte != 10 && readByte != 3) {
        IDValue[i] = readByte;
        i++;
        }
    }
     Serial.println("read tag.");
  }
 
}*/

boolean rfid::dataCheck(){
  int tmp = 0,i;
  for (i = 0; i < dataNum; i++) {
    tmp ^= data[i];
  }

  if (tmp == checksum) return true;
  else return false;
}

boolean rfid::charToHex() {
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

void rfid::setID(char* id){
 strncpy(IDValue, id, idLen);
}

char* rfid::getID(){
  return IDValue;
}

void rfid::printValue() {
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

void rfid::clearValue(){
  char base_char[idLen] = { 0 };
  strncpy(IDValue, base_char, idLen);
  for (int i = 0; i<dataNum; i++) data[i] = 0;
  checksum = 0;
}
