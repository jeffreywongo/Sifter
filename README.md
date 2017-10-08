# Sifter
This android application polls a gmail inbox folder and notifies the user of any new messages. 
It specifically uses the Gmial API.

# Prerequisites
This is an android application that is not on the playstore. In order to run the app 
you will have to install Android Studio on your machine. 
You will also need an android device or an emulator. Instructions for creating an emulator in 
Android Studio are below.

# Installing
Once it is installed, open up Android Studio, click on file, select New, select Project from Version Control, and select Git. 
Get the URL of this repository and paste it in the required field 
Choose a parent directory, create a directory name, and click clone. 
Click the green play button to run the file. 
Select either a connected android device or an enabled emulator. 
To create an emulator, click the AVD manager button on the top. 
Click create virtual device and follow the steps. 
Make sure to clone the emulator you edited. 
Now your emulator is ready to use.

# Results
Ideally, the app will ask for your gmail account to log into. It will then start a background service that 
constantly polls your gmail inbox. If there is a new message, the app creates a notification (not a push), 
saves the information of the message to a local database, and updates the list of messages in the GmailActivity.

# Current issues
The service sometimes randomly gets terminated and doesn't restart like it's supposed to. Other times it takes too long to  restart. 

# Final thoughts
Although it has its shortcomings, I am proud to say that this is my first real personal project. Having to learn android 
development from scratch and working with the Google API was definitely challenging. Nevertheless, I have learned a good 
deal from this project and enjoyed the both fun and frustrating aspects of this journey. I obviousy still have a lot to learn 
and any form of feedback would be greatly appreciated.
