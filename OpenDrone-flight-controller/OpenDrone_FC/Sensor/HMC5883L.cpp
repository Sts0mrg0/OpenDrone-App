/*
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 * Purpose: TODO
 *
 * 	@author Tim Klecka
 * 	@version 0.0.1 07.01.2019
 */
#include "HMC5883L.h"
#include <wiringPi.h>
#include <wiringPiI2C.h>
#include <iostream>
using namespace std;

#define DEVICE_ADDRESS 0x1E
#define MAG_X 0x03
#define MAG_Y 0x05 
#define MAG_Z 0x07

HMC5883L::HMC5883L()
{
	this->fd = wiringPiI2CSetup(DEVICE_ADDRESS);
	if (this->fd < 1) {
		cout << "wiringPiI2CSetup(addressMagnetometer)\n";
		exit(1);
	}
	wiringPiI2CWriteReg8(this->fd, 0x02, 0x00);
}

short HMC5883L::readRawData(int addr)
{
	short high_byte, low_byte, value;

	high_byte = wiringPiI2CReadReg8(fd, addr);
	low_byte = wiringPiI2CReadReg8(fd, addr + 1);
	value = (high_byte << 8) | low_byte;
	return value;
}

double *HMC5883L::getMagnetometerValues()
{
	static double ar[4];  /* Declared locally, but saved in the data-segment (and not on the stack) */
	ar[0] = millis();
	ar[1] = readRawData(MAG_X); //Magnet X
	ar[2] = readRawData(MAG_Y); //Magnet Y
	ar[3] = readRawData(MAG_Z); //Magnet Z
	return ar;
}

HMC5883L::~HMC5883L()
{
}
