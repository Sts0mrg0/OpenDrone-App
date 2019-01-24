/*
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 * Purpose: TODO
 *
 * 	@author Tim Klecka
 * 	@version 0.0.1 07.01.2019
 */

#include "TCPServer.h"
#include "Modbus.h"
#include <stdio.h>  
#include <string.h>   //strlen  
#include <stdlib.h>  
#include <errno.h>  
#include <unistd.h>   //close  
#include <arpa/inet.h>    //close  
#include <sys/types.h>  
#include <sys/socket.h>  
#include <netinet/in.h>  
#include <sys/time.h> //FD_SET, FD_ISSET, FD_ZERO macros  
#include <sstream>
#include <typeinfo>
#include <stdbool.h>

#define TRUE   1  
#define FALSE  0  
#define PORT 2018  


int opt = TRUE;
int master_socket, addrlen, new_socket, client_socket[30],
max_clients = 30, activity, i, valread, sd;
int max_sd;
struct sockaddr_in address;
char buffer[1025];  //data buffer of 1K 

char* Temp;

//set of socket descriptors  
fd_set readfds;

char *message = "Hello Client...";

TCPServer::TCPServer()
{
}


TCPServer::~TCPServer()
{
}

void TCPServer::startUp() {

    //initialise all client_socket[] to 0 so not checked  
    for (i = 0; i < max_clients; i++)
    {
        client_socket[i] = 0;
    }

    //create a master socket  
    if ((master_socket = socket(AF_INET, SOCK_STREAM, 0)) == 0)
    {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    //set master socket to allow multiple connections ,  
    //this is just a good habit, it will work without this  
    if (setsockopt(master_socket, SOL_SOCKET, SO_REUSEADDR, (char *)&opt,sizeof(opt)) < 0)
    {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }

    //type of socket created  
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(PORT);

    //bind the socket to localhost port 2018  
    if (bind(master_socket, (struct sockaddr *)&address, sizeof(address)) < 0)
    {
        perror("bind failed");
        exit(EXIT_FAILURE);
    }
    printf("Listener on port %d \n", PORT);

    //try to specify maximum of 3 pending connections for the master socket  
    if (listen(master_socket, 5) < 0)
    {
        perror("listen");
        exit(EXIT_FAILURE);
    }

    //accept the incoming connection  
    addrlen = sizeof(address);
    puts("Waiting for connections ...");
    this->acceptClients();
}

void TCPServer::acceptClients()
{
    while (TRUE)
    {
        //clear the socket set  
        FD_ZERO(&readfds);

        //add master socket to set  
        FD_SET(master_socket, &readfds);
        max_sd = master_socket;

        //add child sockets to set  
        for (i = 0; i < max_clients; i++)
        {
            //socket descriptor  
            sd = client_socket[i];

            //if valid socket descriptor then add to read list  
            if (sd > 0)
                FD_SET(sd, &readfds);

            //highest file descriptor number, need it for the select function  
            if (sd > max_sd)
                max_sd = sd;
        }

        //wait for an activity on one of the sockets , timeout is NULL ,  
        //so wait indefinitely  
        activity = select(max_sd + 1, &readfds, NULL, NULL, NULL);

        if ((activity < 0) && (errno != EINTR))
        {
            printf("select error");
        }

        //If something happened on the master socket ,  
        //then its an incoming connection  
        if (FD_ISSET(master_socket, &readfds))
        {
            if ((new_socket = accept(master_socket,(struct sockaddr *)&address, (socklen_t*)&addrlen)) < 0)
            {
                perror("accept");
                exit(EXIT_FAILURE);
            }

            //send new connection greeting message  
            if (this->sendMessage(new_socket, message)==0)
            {
                perror("send");
            }

            puts("Welcome message sent successfully");


            //add new socket to array of sockets  
            for (i = 0; i < max_clients; i++)
            {
                //if position is empty  
                if (client_socket[i] == 0)
                {
                    client_socket[i] = new_socket;
                    printf("Adding to list of sockets as %d\n", i);

                    break;
                }
            }
        }
        this->getTemp();
        valread = read(new_socket, buffer, 1024);
        Modbus *mb = new Modbus();
        mb->Interpret(buffer);
        //this->sendMessage(new_socket, Temp);
        //printf("VALUE:%s\n", buffer);
        

        //else its some IO operation on some other socket 
        for (i = 0; i < max_clients; i++)
        {
            sd = client_socket[i];
            if (FD_ISSET(sd, &readfds))
            {
                
                //Check if it was for closing , and also read the  
                //incoming message

                if ((valread = read(sd, buffer, 1024)) == 0)
                {
                    //Somebody disconnected , get his details and print  
                    getpeername(sd, (struct sockaddr*)&address, \
                        (socklen_t*)&addrlen);
                    printf("Host disconnected , ip %s , port %d \n",
                        inet_ntoa(address.sin_addr), ntohs(address.sin_port));

                    //Close the socket and mark as 0 in list for reuse  
                    close(sd);
                    client_socket[i] = 0;
                }

                //Echo back the message that came in  
                else
                {
                    //set the string terminating NULL byte on the end  
                    //of the data read  

                    buffer[valread] = '\0';
                    //this->sendMessage(sd, buffer);
                }
            }
        }
    }
}


int TCPServer::sendMessage(int sd, char* msg) {
    return send(sd, msg, strlen(msg), 0);
}

void TCPServer::getTemp() {
    FILE *temperatureFile;
    double T;
    temperatureFile = fopen("/sys/class/thermal/thermal_zone0/temp", "r");
    if (temperatureFile == NULL)
        ; //print some message
    fscanf(temperatureFile, "%lf", &T);
    T /= 1000;
    fclose(temperatureFile);

    std::stringstream ss;
    ss << "1;" << T << "*";
    Temp = (char*)(ss.str().c_str());

    this->sendMessage(new_socket, Temp);
}