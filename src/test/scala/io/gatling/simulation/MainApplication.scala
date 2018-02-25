package io.gatling.simulation

import io.gatling.tensorflow.TensorflowServingClientProtocol
import io.grpc.netty.NettyChannelBuilder
import tensorflow.serving.PredictionServiceGrpc

object MainApplication extends App {

  val host = "127.0.0.1"
  val port = 9000
  val channel = NettyChannelBuilder.forAddress(host, port)
    .usePlaintext(true)
    .maxMessageSize(200 * 1024 * 1024)
    .build()

  val blockingStub = PredictionServiceGrpc.newBlockingStub(channel)

  val models = List("model1")

  val tfServingClientProtocol =
    new TensorflowServingClientProtocol(channel, blockingStub, models)

  tfServingClientProtocol.call()
  tfServingClientProtocol.shutdown()
}