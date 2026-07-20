Spring boot + rabbit MQ Flow for JSON Message
<!-- How to create Multiple Queue  -->


Core Architecture 
Producer
Consumer 
Queue
Exchange 
Binding
Routing Key
Message


Producer -> RabbitMQ Message Broker -> Consumer
What is message queue

Message queuing allows applications to communicate by sending messages to each other. The 
message queue provides temporary message storage when the destination program is busy or not connected

A message queue is made up of producer, a broker(the message queue software), and a consumer.

A message queue provides an asynchronous communication between applications.


Rabbit MQ
Rabbit Mq is a message queue software (message broker/queue manager) that acts as an intermediary platform where different applications can send and receive messages.

RabbitMQ originally implements the advanced message queuing protocol (AMQP).
But now RabbitMQ also supports several other API protocols such as STOMP, MQTT and HTTP.