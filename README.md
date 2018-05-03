# Concurrent programming in Java

This project demonstrates different approaches to asynchronous programming in Java.

# Branches

* **master** - threadpools, futures
* **non-blocking** - use apache async http client (with AsyncRestTemplate) which uses a fixed low number of threads to reach concurrency
* **callback_hell** - demonstrates the problem of embedded callbacks
* **webclient** - uses Spring 5 reactive web client which also leverages the power of reactive stream library called Reactor

The examples assume that there is a running server on *localhost:8080* which has a */slow* endpoint. For this, you can use another example: https://github.com/martin-tarjanyi/webflux-demo
