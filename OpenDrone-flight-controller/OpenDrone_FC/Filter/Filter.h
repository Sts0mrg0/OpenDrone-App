/*
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 * Purpose: TODO
 *
 * 	@author Tim Klecka
 * 	@version 0.0.1 07.01.2019
 */
#include <list> 
#pragma once
class Filter
{
public:
	Filter(double minValue, double maxValue, double minSize);
	~Filter();
	double addValue(double value);

private:
	std::list<double> list1;
	double maxValue, minValue;
	double minSize;
};
