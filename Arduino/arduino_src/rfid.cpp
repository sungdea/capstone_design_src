#include <rfid.h>

rfid::rfid(){
   char base_char[idLen] = {0};
   strncpy((char *)IDValue, base_char, idLen);
   rSerial.begin(9600);
}

rfid::rfid(unsigned char id[idLen]){
   strncpy((char *)IDValue, (char *)id, idLen-1);
   rSerial.begin(9600);
}

void rfid::readtag(){
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
        IDValue[i] = readByte;
        i++;
        }
    }
  }
}

void rfid::checksum(){

}
