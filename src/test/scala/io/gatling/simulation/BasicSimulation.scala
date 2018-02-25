package io.gatling.simulation

import io.gatling.core.Predef.Simulation
import io.gatling.core.Predef._
import io.gatling.tensorflow.{TensorflowServingClientBuilder, TensorflowServingClientProtocol}
import io.grpc.netty.NettyChannelBuilder
import tensorflow.serving.PredictionServiceGrpc

class BasicSimulation extends Simulation {

  val host = "127.0.0.1"
  val port = 9000
  val channel = NettyChannelBuilder.forAddress(host, port)
    .usePlaintext(true)
    .maxMessageSize(200 * 1024 * 1024)
    .build()

  val blockingStub = PredictionServiceGrpc.newBlockingStub(channel)

  val models = List("model1")

  val inputParam = "images"
  val outputParam = "scores"

  val tfServingClientProtocol =
    new TensorflowServingClientProtocol(channel, blockingStub, models, inputParam, outputParam)

  val scn = scenario("Tensorflow Serving Client call").exec(TensorflowServingClientBuilder())

  setUp(scn.inject(atOnceUsers(1))).protocols(tfServingClientProtocol)
}