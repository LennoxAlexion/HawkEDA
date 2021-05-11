<img src="/icon/HawkEDA.png" alt="HawkEDA" width="250"/>

A tool for quantifying data integrity violations in event-driven microservices.

#

## Getting Started
### Prerequisites

* [Java SE Development Kit 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)

* [Maven](https://maven.apache.org/)

To run the tool against an example event-driven application, we will separately deploy the modified version of a popular open-source application [eShopOnContainers](https://github.com/LennoxAlexion/eShopOnContainers/tree/modified-for-analysis). You can use the original project, but please take care of authentication and updating catalog items in the example scenario provided with the tool. This step can be performed after installing the tool as well.

* Install [Docker Compose](https://docs.docker.com/compose/)

* Configure the application by following the original [guidelines](https://github.com/dotnet-architecture/eShopOnContainers/wiki/Windows-setup#configure-docker)

* Run the below command from the **`/src/`** directory of eShopOnContainers to deploy necessary microservices for the example scenarios.
```
docker-compose up -d webmvc
```

### Building from source

Configure the [property file](https://github.com/LennoxAlexion/HawkEDA/blob/master/src/main/resources/HawkEDA.properties) to set up RabbitMQ connector (as per the external event driven application), logger and max thread pool allowed for the scenarios.
```bash
#RabbitMQ Connection prop:
rabbitmq.username=guest
rabbitmq.password=guest
rabbitmq.virtualhost=/
rabbitmq.hostname=localhost
rabbitmq.port=5672

#Logger prop:
#Path separator expected at the end. Specify as per the OS path separators; otherwise, the default path will be used.
logger.path=/home/HawkEDA/

#ThreadPool:
executors.nThreads=100
```

Build and package the project. This generates a package with required dependencies at ***`/HawkEDA/target/HawkEDA-1.0-SNAPSHOT-jar-with-dependencies.jar`***

```
mvn clean package
```

## Running the tool

```
java -cp /HawkEDA/target/HawkEDA-1.0-SNAPSHOT-jar-with-dependencies.jar HawkEDA SCENARIO_CLASS_NAME [arguments]
```

Executing scenarios included in the tool:
```
java -cp /HawkEDA/target/HawkEDA-1.0-SNAPSHOT-jar-with-dependencies.jar HawkEDA CheckoutWhilePriceUpdateScenario 500 zipfian 25
```
```
java -cp /HawkEDA/target/HawkEDA-1.0-SNAPSHOT-jar-with-dependencies.jar HawkEDA OutOfStockCheckoutScenario 500 uniform 100
```
```
java -cp /HawkEDA/target/HawkEDA-1.0-SNAPSHOT-jar-with-dependencies.jar HawkEDA SingleBasketMultipleCheckoutScenario 10
```

Execution Result and Logs:
After the execution, result and event log will be stored in the path specified in the property files or the default path `\HawkEDA\logs`.

Format of the log files:

## Defining new scenarios

## Built Using
* [Esper](https://www.espertech.com/esper/) - Complex Event Processor
* [YCSB](https://github.com/brianfrankcooper/YCSB/tree/master/core/src/main/java/site/ycsb/generator) - Distribution generator for the workload
* [Hawk Icon](https://www.deviantart.com/qsc123951/art/Hawk-Vr-1-5-380179380) by [deviantART:qsc123951](https://www.deviantart.com/qsc123951/about)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE) file for details

## Acknowledgments

It could not have been possible without the guidance of:
* [Rodrigo Laigner](https://github.com/rnlaigner)
* [Yongluan Zhou](https://ylzhou.github.io/)

