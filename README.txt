Jessie McGarry - jmcgarry3@gatech.edu


FILES:
Server/		- The source code and makefile for the TCP Server.
LetMeIn/	- The source code and necessary files for the Android Client
LetMeIn.apk     - A compiled version of the Android client, which can easily be run from an Android device.
README.txt 	- this

INSTRUCTIONS:

Running the Server:
1. in the 'Server' directory, type 'make'
2. the executables for all of the programs will be created in the same directory
3. type 'make clean' to delete the executables
4. Run the server executable in the form "./server-tcp <Port to run on> <username to support> <Password to support>

Running the Client:
In addition to source code which can be run from eclipse, I have included a .apk file, and there are two options for installing these on an android device.

Installing Applications Through the Android Market:
Copy the APK file to your Android’s memory card and insert the card into your phone.
Download and install the Apps Installer application from the Android Market
Once installed, the Apps Installer will display the APK files on the memory card.
Click and install your APK files.

Installing Applications With Android SDK:
Download and install the Google Android SDK program and the Android USB drivers. 
The download links are as follows: http://code.google.com/android/intro/installing.html
http://dl.google.com/android/android_usb_windows.zip
You need to modify your Android’s settings to allow the installation of applications from other sources. Under “Settings,” select “Application Settings” and then enable “Unknown Sources.” 
Also under “Settings,” select “SD Card” and “Phone Storage,” and finally enable “Disable Use for USB Storage”
This last step is easy. Open Command Prompt and type the following: adb install <1>/<2>.apk
However, when you type the command, replace <1> with the path to your APK file and replace <2> with the name of the APK file.
You’re done! Your application is now ready for your use and enjoyment.

These instructions were found at http://www.talkandroid.com/guides/beginner/install-apk-files-on-android/


APPLICATION PROTOCOL:
The request/response format is <message><sp><parameters>
Additional parameters are specified using additional spaces

The general sequence for both programs is as follows:
1. Client sends the text "requesting_authentication" to the server
2. Server responds with "requesting_hash <random, lower case 64 character string>"
3. Client responds with "username_and_hash: <username> <random string> <hash>"
4. Server responds with "successful_login", "user_not_found", or "incorrect_password"
5. If the authentication is successful, the server is now accepting requests from the client. If the server receives "requesting_file_contents", it will either respond with the contents of a file line-by-line, "file_not_found", or "empty_file". It will then send "end_of_message" (which is used on the client side so the client knows when to stop reading lines).
6. The server will continue to accept requests until the client sends the "close" command, which indicates that it is done and the server can close the socket.

LIMITATIONS:
1. There is no threading, so the server can process only one client at a time. In addition to inefficiency, this means that if a client does not logout when they are done the server is tied up, waiting for the client to send the "close" message. I could change this with threading or a timer.
