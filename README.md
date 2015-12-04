# Chat Intranet
This repository contains a multi-language (English, French, Romanian for now) chat application made in Java. 

### Functions ###
An user can create a hosted discussion group or join a group that is already made.
There are two types of groups: public and private (password protected). When connected to a
group, an user can send private message to another user or send files.

### Details ###
Finding of a server is realised with UDP sockets, while the sending of messages inside 
of a group is realised with TCP sockets.
Data sent through the sockets is protected by an ecryption AES 128 bits.
The traffic made/as well as the messages sent throw out the network can be found in the log files.

### Build ###
The project can be built in any Java integrated development environment (preferably Eclipse).

### Faq ###
For more details regarding the application or would you wish to provide me with some feedback, don't hesitate to contact me at:    
stancioiu.razvan@gmail.com
