Simple HTTP Server
=============================
A simple HTTP server written in Java.This server does not utilise the 
HTTP protocol in fully. It can generate few error messages and only serves 
static files.


Porting to Python
============================
Plans to port this server to Python are underway

Browser support
=============================
Apparently Firefox is the only browser that works well with this server.
Safari has minimal compatibility and Chrome sucks.
For the website that i was using for testing,Safari and Chrome kept requesting
for favicon.ico files which were nowhere to be found and the requests kept on
failing even though the contact was made,but again this is just for the purpose of
learning and it's not like we wanna challenge Nginx or Apache so just use FIREFOX 
and you will be safe.