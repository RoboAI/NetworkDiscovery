# NetworkDiscovery
Network Discovery with UDP comms

*Not yet complete*

Starting from MyCustomView should give an idea of how code is linked together.

Currently two apps can communicate with each other via WIFI moving a circle according to the position on the other phone.

The app uses UDP to send the messages. Multi-threading is used to make the connection and move the circle around to avoid hanging of the UI.

The MyCustomView handles the touch-events, displays the circle and triggers the .notifyAll() when there is a finger-movement.
NDClient uses .wait() to wait for MyCustomView to send the .notifyAll() and sends the new coordinates over the network.

MyCustomView registers itself with NDServer to recieve the coordinates of the circle. NDServer also uses another thread to handle network-comms.


