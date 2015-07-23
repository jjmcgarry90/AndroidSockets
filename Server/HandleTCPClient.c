#include <stdio.h>      /* for printf() and fprintf() */
#include <sys/socket.h> /* for recv() and send() */
#include <unistd.h>     /* for close() */
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include "protocol.h"
#include "md5.h"

#define RANDBUFSIZE 64
#define LINEBUFSIZE 200

void DieWithError(char *errorMessage);  /* Error handling function */
FILE *fr;

void HandleTCPClient(int clntSocket, char* username, char* password)
{
    char inMsg[MSGBUFSIZE+1];             /* Buffer for received string */
    char outMsg[MSGBUFSIZE+1];	          /* Buffer for sent string */
    int bytesRcvd, bytesSent;             /* Size of received message */
    int totalBytesRcvd, totalBytesSent;   /* Total bytes received, sent */
    char randChars[RANDBUFSIZE+1];		  /* Hold random string */
    unsigned int iseed;			  /* Random seed value */
    int expectedMsgLen;			  /* expected response length */
    char clientHash[HASHBUFSIZE];	  /* holds client's hash string */
    char serverHash[HASHBUFSIZE];	  /* holds server's hash string */
    char fileLine[LINEBUFSIZE];
    int done=0;
    
    
    /* usernames and passwords that the system supports */
    char* credentials[6] = {username, password, "Bob", "123", "Sally", "111"};
    
    /*expected length of first message, to ensure we get all the bytes */
    expectedMsgLen = strlen(AUTH_REQUEST); 
    //Receive authentication request
    totalBytesRcvd = 0;
    while (totalBytesRcvd < expectedMsgLen) { //ensure all bytes are received
	 bytesRcvd = recv(clntSocket, inMsg+totalBytesRcvd, MSGBUFSIZE-totalBytesRcvd, 0);
	 if(bytesRcvd!=-1)
    	 	totalBytesRcvd+= bytesRcvd;
    }

    inMsg[totalBytesRcvd] = '\0';	
    printf("Received Message: \"%s\"\n\n", inMsg);   //check
    
    //generate a random 64-character string based on time
    iseed = (unsigned int)time(NULL);
    srand (iseed);
    int i;
    for(i=0; i<RANDBUFSIZE; i++) {
   	 randChars[i] = (char)((rand() % 26)+ 97); // = a random, lower-case letter
    }
  	//add a newline character
    randChars[RANDBUFSIZE] = '\0';	//terminate the string
    strcpy(outMsg, randChars); //copy the random string to the send buffer
    outMsg[RANDBUFSIZE]='\n';
    outMsg[RANDBUFSIZE+1]='\0'; 
    
    //send the random string
    totalBytesSent = 0;
    while (totalBytesSent < strlen(outMsg)) //ensure all bytes are sent
    {
        if ((bytesSent = send(clntSocket, outMsg + totalBytesSent,
	    			    strlen(outMsg) - totalBytesSent, 0)) <= 0)
            DieWithError("send() failed or connection closed prematurely");
        totalBytesSent += bytesSent;   /* Keep tally of total bytes */
    }
    outMsg[RANDBUFSIZE] = '\0'; //get rid of new line character
    printf("Sending random string: \"%s\"\n\n", outMsg);  //check

    //receive the "username:hash" from the Client
    totalBytesRcvd = 0;
    expectedMsgLen = 1+32; //we should at least receive ':' + 32 byte hash string
    while (totalBytesRcvd < expectedMsgLen) { //ensure all bytes are received
	 bytesRcvd = recv(clntSocket, inMsg+totalBytesRcvd, MSGBUFSIZE-totalBytesRcvd, 0);
	 if(bytesRcvd!=-1)
    	 	totalBytesRcvd+= bytesRcvd;
    }

    inMsg[totalBytesRcvd] = '\0';	//terminate the string
    printf("Received Message: \"%s\"\n\n", inMsg); //print received data
    
    //split the string into username and hash
    char* delim = ":";  //delimiter for split
    username  = strtok(inMsg,delim);
    strcpy(clientHash, strtok(NULL, delim));

    printf("Split message into \"%s\" and \"%s\"\n\n", username, clientHash);
    
    //see if username is on file, checking every other string in credentials
    fputs("Checking Username...",stdout);
    int found = 0;
    for (i=0; i<sizeof(credentials)/4; i+=2) //i < number of strings in credentials
    	if(strcmp(username, credentials[i])==0) { //if found
    	    found = 1;
    	    password = credentials[i+1];  //pull the password
    	} 
    
    //if username is valid
    if (found)	{
    	    printf("found %s\n\n",username);
  
	    //concatenate username, password, and 64 character string
	    sprintf(serverHash, "%s", username);
	    sprintf(serverHash+strlen(username), "%s", password);
	    sprintf(serverHash+strlen(username)+strlen(password), "%s", randChars);
//	    serverHash[strlen(username)+strlen(password)+strlen(randChars)]='\n';
//	    serverHash[strlen(username)+strlen(password)+strlen(randChars)+1]='\0';

	    printf("String to be hashed: \"%s\"\n\n", serverHash);
	    
	    //hash the concatenation
	    md5_byte_t* string =  (md5_byte_t*) serverHash;
	    md5_state_t state;
	    md5_byte_t digest[16];
	    myMethod(&state, digest, string);
	    
	     for(i = 0; i < 16; i++)
	    	sprintf(serverHash+(i*2), "%02x", digest[i]);   
	    printf("Hash result: \"%s\"\n\n", serverHash);
	    
	    //compare client and server hash results
	    if (strcmp(serverHash, clientHash)==0) { //if they match
	    	puts("Successful login, notifying client.\n");
	    	/* prepare send buffer */ 
	    	sprintf(outMsg, "%s\n", AUTH_SUCCESS); 
	    }
            else { //username was found, so must be a bad password
            	puts("Bad password, notifying client.\n");
            	/* prepare send buffer */
            	sprintf(outMsg,"%s\n", AUTH_BAD_PASS);
            	done=1;
            }
    }
    
    //username is not valid
    else {
    	puts("Bad username, notifying client.\n");
    	/* prepare send buffer */
    	sprintf(outMsg, "%s\n", AUTH_BAD_USR);
    	done=1;
    }
    
    //send authentication status
    totalBytesSent = 0;
    //outMsg[RANDBUFSIZE-1] = '\n';
    while (totalBytesSent < strlen(outMsg)) //ensure all bytes are sent
    {
        if ((bytesSent = send(clntSocket, outMsg + totalBytesSent,
	    			    strlen(outMsg) - totalBytesSent, 0)) <= 0)
            DieWithError("send() failed or connection closed prematurely");
        totalBytesSent += bytesSent;   /* Keep tally of total bytes */
    }
    
    
    int caseNum;  //which switch case to take
    char* filename; //if file request, name of file to be retrieved
    char* command; //the command given
    
    /* Now that the user has authenticated, we listen for commands and 
    take the appropriate actions. For now, the only commands supported 
    are file requests and close connection. The server will continue to
    listen here until the "close" command is issued by the client. */
    while(!done) {
    	    //get the command message from the server
	    totalBytesRcvd = 0;    
	    while (totalBytesRcvd==0) {
		bytesRcvd = recv(clntSocket, inMsg+totalBytesRcvd, MSGBUFSIZE-totalBytesRcvd, 0);
	 	if(bytesRcvd!=-1)
	    	     totalBytesRcvd+= bytesRcvd;
	    }
	    inMsg[totalBytesRcvd] = '\0'; //terminate the string
	    
	    command = strtok(inMsg,DELIM); //DELIM is space character
	
	    caseNum = 0;
	    if (command != NULL) { //either close the connection or send file contents
	    	if (strcmp(command, CLOSE)==0)
	    		caseNum = 1; 
	    	if (strcmp(command, FILE_REQUEST)==0)
	    		caseNum = 2; 
	    }
	    	
	    switch(caseNum) {
	    case 0:
	    	   break;
	    case 1: //close the connection, and go back to listening for connection requests
	    	   done=1;
	    	   break;
	    case 2: //send file contents to user
	    	   filename = strtok(NULL,delim); //the second parameter is the filename
		   printf("Retrieving File: %s\n\n", filename);
		   fr = fopen (filename, "rt");  // open the file for reading 
		    
		   if (fr == NULL) {   //file not found, notify client
		     sprintf(outMsg, "%s\n", FILE_NOT_FOUND);
		     puts(outMsg);
		     totalBytesSent = 0;
		     while(totalBytesSent < strlen(outMsg)) { // +1 for newline
		           bytesSent = send(clntSocket, outMsg+totalBytesSent, 
		           			 strlen(outMsg)-totalBytesSent, 0);
		           if (bytesSent > 0) totalBytesSent += bytesSent;
		     }
		   }
		   
		   else {  //otherwise send contents line by line to the client
		     strcpy(fileLine,"");   //set fileLine to ""
		     while (fgets (fileLine , LINEBUFSIZE-1 , fr) != NULL ) {
		       fileLine[strlen(fileLine)-1]='\n';
		       fileLine[strlen(fileLine)]= '\0';
		       send(clntSocket, fileLine, strlen(fileLine), 0);
		     }
		     
		     if(strcmp(fileLine,"")==0) {  //if file is empty
		       	   sprintf(fileLine, "%s\n", EMPTY_FILE);
		       	   send(clntSocket, fileLine, strlen(fileLine), 0);
		     }
		     fclose (fr); //close the file handle
		   }
		   
		   //send message terminator to client
		   sprintf(outMsg, "%s\n", EOM); 
		   totalBytesSent=0;
		   while(totalBytesSent < strlen(outMsg)) { 
		   	bytesSent = send(clntSocket, outMsg+totalBytesSent,
		   			 strlen(outMsg)-totalBytesSent, 0);
		   	if (bytesSent > 0) totalBytesSent += bytesSent;
		   }
		   break;
	    }
    }

    
    puts("Finished. Closing connection.");
  	
    close(clntSocket);    /* Close client socket */
}	
