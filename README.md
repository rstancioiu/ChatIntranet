# Chat Intranet
This repository contains a the source code of a chat made in Java Swing.
The project was initially made by myself with 3 other colleagues as a school project.
The final version of the school project can be found on the first commit made.
On this repository, there are two versions of the project:
 * English Version: code written in english (multi-language added)
 * French Version: code written in french with javadoc in french

### Functionalities ###
An user can create a hosted discussion group or join a group that is already made.
There are two types of groups: public and private(password protected). When connected to a
group, an user can send private message to another user or send files via the chat.

### Details ###
Finding of a server is realised with UDP sockets, while the sending of messages inside 
of a group is realised with TCP sockets.
Data sent through the sockets is protected by an ecryption AES 128 bits.

### Build ###
The project can be built in any Java integrated development environment (IDE).

### Faq ###
For questions or suggestions, do not hesitate to contact me at : stancioiu.razvan@gmail.com
