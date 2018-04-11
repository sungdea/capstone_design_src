#ifndef RFID_H
#define RFID_H

#include "arduino.h"
#include <SoftwareSerial.h>
#include <string.h>

const int tagLen = 16;  // RFID tag full length
const int idLen = 13;   // RFID tag ID length
const int RX=2, TX=3, dataNum=5;


class rfid
{
  private:
    char IDValue[idLen];
    int data[dataNum], checksum;
  public: 
    rfid();
    boolean dataCheck();
    boolean charToHex();
    void readTag(SoftwareSerial &rSerial);
    char* getID();
    void printValue();
    void clearValue();
};


#endif
