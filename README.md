tensorflow-serving-gatling-extension
====================================

[Gatling](http://gatling.io/#/) is an open-source load testing framework. The `Tensorflow-serving Gatling extension` can be used for stress testing an existing Tensorflow-serving installation using `Gatling`.

## Compatibility

The extension uses latest released version of Gatling `2.2` and latest version of Grpc libraries `1.10.0`.

## Installation

### Installation from source

```
mvn clean install -Ppackage-only
```

## Getting Started

### Prerequisites

- Install `tensorflow-serving` following [link](https://wwww.tensorflow.org/serving/setup)

### Steps

- Start & load a simple tensorflow-serving server by following the steps.

```
cd src/test/resources
tensorflow_model_server --port=9000 --model-config-file=models.conf
```

- Run a simple load test using sample data.

```
mvn gatling:execute -Dgatling.simulationClass=io.gatling.simulation.BasicSimulation
```

## Parameters

- `host` : The tensorflow-serving host to which gatling will fire requests.

- `port` : The tensorflow-serving port to which gatling will fire requests.

- `models` : A list of models & their corresponding versions hosted by tensorflow-serving to which gatling will fire requests.

- `inputParam` : The input Parameter to run the model.

- `outputParam` : The output Parameter which gives the results.

## Examples

- To run multiple requests on a single model `io.gatling.simulation.BasicSimulation.scala`
- To run multiple requests on multiple models `io.gatling.simulation.MultiModelSimulation.scala`

## Todos

- Currently, the plugin is tightly coupled for testing `mnist models` only. If there are feature requests, this project can be made
more generic.
