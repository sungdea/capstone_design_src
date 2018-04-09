#ifndef RFID_H
#define RFID_H

#include "arduino.h"
#include <string.h>
#include <SoftwareSerial.h>

const int tagLen = 16;  // RFID tag full length
const int idLen = 13;   // RFID tag ID length
const int RX=2, TX=3;

SoftwareSerial rSerial(2,3); // RX, TX

class rfid
{
  private:
    unsigned char IDValue[idLen];
  public:
    rfid();
    rfid(unsigned char id[idLen]);
    void readtag();
    void checksum();
};


#endif
