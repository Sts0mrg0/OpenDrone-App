#include "Modbus.h";
#include "iostream";
#include <string.h>;
#include <iostream>
#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <sstream>

using namespace std;

Modbus::Modbus()
{
}

Modbus::~Modbus()
{
}

void Modbus::Interpret(string str)
{
    //cout << str << " | ";
    std::stringstream ss(str);
    vector<string> result;

    while (ss.good()) 
    {
        string substr;
        getline(ss, substr, ';');
        result.push_back(substr);
    }

    //if(result.size)
    //cout << "--------------------------------Packets:" << result.size();
    int i;
    cout << endl;
    int slaveID = stoi(result.at(1));

    //Amount of commands
    int packages = stoi(result.at(2));

    for (i = 0; i < packages; i++) {
        int functionCode = stoi(result.at(3+(i*3)));
        string data = result.at(4+(i*3));
        string parity = result.at(5+(i*3));
        string am = "Packages: "+ to_string(packages) + "SlaveID: " + to_string(slaveID) + "(1)" + ", FC: " + to_string(functionCode) + "("  +to_string(3+(i*3)) +")" + ", Data: " + data + "(" + to_string(4 + (i * 3)) + ")" + ", parity: " + parity + "(" + to_string(5 + (i * 3)) + ")";
        cout << am << " / ";
    }
    cout << endl;
    /*for (i = 0; i < result.size(); i++) {
        cout << result.at(i) << std::endl;
    }*/
    cout.flush();
}
