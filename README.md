# Zookeeper
A simple bank management system which uses Zookeeper for ensuring consistency, availability and fault tolerance

## Aim

The aim of the project is to create a simple distributed bank system made of application servers connected to a Zookeeper ensemble (or standalone if locally). Client requests (in this case STDIN commands) can be sent to any application server. In case the request is sent to the leader elected amongst the application servers, the request is processed by that server and sent forwarded to all the other applications servers. In case of a request sent to a non-leader application server, however, there are 2 cases: *_read_* requests are processed by the application server itself; *_create_*, *_update_* and *_delete_* requests are forwarded to the application server leader, which processes it and, when terminated, forwarded to all the other application servers.

# Communication

Communication never happens directly between application servers. Each of the application server is connected to a Zookeper ensemble which functions like a service bus. For this purpose, znodes can be used as

![](images/znodes.png?raw=true)

