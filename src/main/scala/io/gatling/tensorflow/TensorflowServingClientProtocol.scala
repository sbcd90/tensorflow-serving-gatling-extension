package io.gatling.tensorflow

import java.util.concurrent.TimeUnit

import com.google.protobuf.Int64Value
import io.gatling.core.protocol.Protocol
import io.grpc.ManagedChannel
import org.tensorflow.framework.{DataType, TensorProto, TensorShapeProto}
import tensorflow.serving.{Model, Predict, PredictionServiceGrpc}

class TensorflowServingClientProtocol(channel: ManagedChannel,
                                      blockingStub: PredictionServiceGrpc.PredictionServiceBlockingStub,
                                      models: List[(String, Int)],
                                      inputParam: String,
                                      outputParam: String,
                                      imagePath: String = TensorflowMnistDataReader.IMAGE_FILE_PATH,
                                      labelPath: String = TensorflowMnistDataReader.LABEL_FILE_PATH)
  extends Protocol {

  def call(): Unit = {
    models.foreach(predict(_))
  }

  def shutdown(): Unit = {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
  }

  private def predict(model: (String, Int)): Unit = {
    val images = TensorflowMnistDataReader.getImages(imagePath)
    val labels = TensorflowMnistDataReader.getLabels(labelPath)

    for (i <- images.indices) {
      val imageTensor = createImageTensor(images(i))
      if (imageTensor != null) {
        if (!requestService(imageTensor, labels(i), model)) {
          throw new RuntimeException("Prediction call failed")
        }
      }
    }
  }

  private def createImageTensor(image: Array[Array[Int]]): TensorProto = {
    val featuresDim1 = TensorShapeProto.Dim.newBuilder()
        .setSize(1).build()
    val featuresDim2 = TensorShapeProto.Dim.newBuilder()
        .setSize(image.length * image.length).build()

    val imageFeatureShape = TensorShapeProto.newBuilder()
        .addDim(featuresDim1).addDim(featuresDim2).build()

    val imageTensorBuilder = TensorProto.newBuilder()
    imageTensorBuilder.setDtype(DataType.DT_FLOAT).setTensorShape(imageFeatureShape)

    for (i <- image.indices) {
      for (j <- image.indices) {
        imageTensorBuilder.addFloatVal(image(i)(j))
      }
    }

    imageTensorBuilder.build()
  }

  private def requestService(imagesTensorProto: TensorProto, label: Int, model: (String, Int)): Boolean = {
    val version = Int64Value.newBuilder().setValue(model._2).build()

    val modelSpec = Model.ModelSpec.newBuilder()
      .setName(model._1)
      .setVersion(version)
      .setSignatureName("predict_images")
      .build()

    val request = Predict.PredictRequest.newBuilder()
      .setModelSpec(modelSpec)
      .putInputs(inputParam, imagesTensorProto)
      .build()

    val response = blockingStub.withDeadlineAfter(10, TimeUnit.SECONDS).predict(request)
    val scores = response.getOutputsMap.get(outputParam)

    if (scores.getFloatValList.size() > 0) {
      return true
    }
    false
  }
}