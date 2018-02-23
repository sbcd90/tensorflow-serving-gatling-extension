package io.gatling.tensorflow

import java.util.concurrent.TimeUnit

import com.google.protobuf.Int64Value
import io.gatling.core.protocol.Protocol
import io.grpc.ManagedChannel
import org.tensorflow.framework.{DataType, TensorProto, TensorShapeProto}
import tensorflow.serving.{Model, Predict, PredictionServiceGrpc}

import scala.util.Random

class TensorflowServingClientProtocol(channel: ManagedChannel,
                                      blockingStub: PredictionServiceGrpc.PredictionServiceBlockingStub,
                                      models: List[String])
  extends Protocol {

  def call(): Unit = {
    predict(models(Random.nextInt(models.length - 1)))
  }

  private def shutdown(): Unit = {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
  }

  private def predict(modelId: String): Unit = {
    val images = TensorflowMnistDataReader.getImages()
    val labels = TensorflowMnistDataReader.getLabels()

    for (i <- 0 until 10) {
      val imageTensor = createImageTensor(images(i))
      if (imageTensor != null) {
        requestService(imageTensor, labels(i), modelId)
      }
    }
  }

  private def createImageTensor(image: Array[Array[Int]]): TensorProto = {
    try {
      val featuresDim1 = TensorShapeProto.Dim.newBuilder()
          .setSize(1).build()
      val featuresDim2 = TensorShapeProto.Dim.newBuilder()
          .setSize(image.length * image.length).build()

      val imageFeatureShape = TensorShapeProto.newBuilder()
          .addDim(featuresDim1).addDim(featuresDim2).build()

      val imageTensorBuilder = TensorProto.newBuilder()
      imageTensorBuilder.setDtype(DataType.DT_FLOAT).setTensorShape(imageFeatureShape)

      for (i <- 0 until image.length) {
        for (j <- 0 until image.length) {
          imageTensorBuilder.addFloatVal(image(i)(j))
        }
      }

      imageTensorBuilder.build()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        null
    }
  }

  private def requestService(imagesTensorProto: TensorProto, label: Int, modelId: String): Unit = {
    val version = Int64Value.newBuilder().setValue(1).build()

    val modelSpec = Model.ModelSpec.newBuilder()
      .setName(modelId)
      .setVersion(version)
      .setSignatureName("predict_images")
      .build()

    val request = Predict.PredictRequest.newBuilder()
      .setModelSpec(modelSpec)
      .putInputs("images", imagesTensorProto)
      .build()

    val response = blockingStub.withDeadlineAfter(10, TimeUnit.SECONDS).predict(request)
    val scores = response.getOutputsMap().get("scores")

    if (scores.getFloatValList().size() > 0) {
      System.out.println("Prediction request successful")
    }
  }
}