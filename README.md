# Chat server project

## Description

* Server application that allows to send and receive simple chat messages.
* Implemented with:
  * Spring Boot 2.7.0 using WebFlux
  * RSocket
  * Redis pub/sub
  * MongoDB

## Local environment

* Docker containers for MongoDB & Redis started with docker-compose: ```docker-componse up -d```

**Note: these containers must be running for integration tests**

## REST API

* Rooms:
  * Read room list: GET /rooms
  * Create room: POST /rooms
* Messages:
  * Read room messages: GET /room/{roomId}/messages
  * Post message to a room: POST /room/{roomId}/messages
  * Subscribe to room messages: GET /room/{roomId}/subscribe
  * Post private message: POST /room/{username}/privatemessages

## RSocket endpoints

* Read room messages: ws://localhost:7001/roommessages
* Read privata messages: ws://localhost:7001/chatmessages