# NetworkDiscovery (Java/Android)
Network Discovery with UDP comms

*Not yet complete*

Starting from MyCustomView.java should give an idea of how code is linked together.

Currently two apps can communicate with each other via WIFI, moving a circle according to the position on the other phone.

The app uses UDP to send the messages. Multi-threading is used to make the connection and to avoid hanging of the UI.

The MyCustomView handles the touch-events, displays the circles and triggers the .notifyAll() when there is a finger-movement.
NDClient uses .wait() to wait for the .notifyAll() and sends the new coordinates over the network.

MyCustomView registers itself with NDServer to recieve the coordinates of the circle. NDServer uses a separate thread to handle network-comms.

## Overview of sending 'x,y coordinates'

<img src="https://wvlkya.am.files.1drv.com/y4mghvczFXQmj2ApxYqShSfl-uDIWmIIZnw8K4Rfi8Lzs1smIzMSAG38HhtxYDUrcGEjRaSx64e2WOk-Vfnr13TBILSfjtxgN4vjqgjPg96f2gnfYhGZLdwHxScH3I10B0Q97pPBMx77RRJQmkyDZVf6KOCKGhst4NQyJYI-tUwYjdsDgIJPKIkNFIgipGs_36WRZL7nrlODLn9B078W7mdcA?width=810&height=368&cropmode=none" width="500">


