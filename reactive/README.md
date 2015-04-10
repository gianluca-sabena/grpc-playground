A first simple example on how to use Goggle gRPC in scala with RxScala

# Requirements

See main [README.md](../README.md)

# How to run

Open two terminal and start one server and one or more clients

* in one terminal ```sbt run``` (select server) 
* in second terminal ```sbt run``` (select client) 


# Benchmark

Experimental use of http://scalameter.github.io to measure client request performances

* In one terminal ```sbt reStart``` (select server) 
* In a second terminal ```sbt test``` 

# To do

* Catch back pressure exception
* Move non blocking stream from thread to to threadpool 

# Resources

http://scalameter.github.io/home/gettingstarted/0.7/configuration/index.html
https://github.com/jhusain/learnrxjava/tree/master/src/main/java/learnrxjava/examples
https://groups.google.com/forum/#!forum/scalameter


