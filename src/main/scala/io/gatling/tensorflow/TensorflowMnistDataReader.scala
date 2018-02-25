package io.gatling.tensorflow

import java.io.{ByteArrayOutputStream, RandomAccessFile}
import java.nio.ByteBuffer

object TensorflowMnistDataReader {
  val LABEL_FILE_MAGIC_NUMBER = 2049
  val IMAGE_FILE_MAGIC_NUMBER = 2051

  val LABEL_FILE_PATH = "mnist_test_data/t10k-labels-idx1-ubyte/data"
  val IMAGE_FILE_PATH = "mnist_test_data/t10k-images-idx3-ubyte/data"

  def getLabels(): Array[Int] = {
    val bb = loadFileToByteBuffer(LABEL_FILE_PATH)

    assertMagicNumber(LABEL_FILE_MAGIC_NUMBER, bb.getInt())

    val numLabels = bb.getInt()
    val labels = Array.ofDim[Int](numLabels)

    for (i <- 0 until numLabels) {
      labels(i) = bb.get() & 0xFF
    }
    labels
  }

  def getImages(): List[Array[Array[Int]]] = {
    val bb = loadFileToByteBuffer(IMAGE_FILE_PATH)

    assertMagicNumber(IMAGE_FILE_MAGIC_NUMBER, bb.getInt())

    val numImages = bb.getInt()
    val numRows = bb.getInt()
    val numColumns = bb.getInt()
    var images = List[Array[Array[Int]]]()

    for (i <- 0 until numImages) {
      images = images ++ List(readImage(numRows, numColumns, bb))
    }
    images
  }

  private def readImage(numRows: Int, numColumns: Int, bb: ByteBuffer): Array[Array[Int]] = {
    val image = Array.ofDim[Int](numRows, 1024)
    for (row <- 0 until numRows) {
      image(row) = readRow(numColumns, bb)
    }
    image
  }

  private def readRow(numCols: Int, bb: ByteBuffer): Array[Int] = {
    val row = Array.ofDim[Int](numCols)
    for (col <- 0 until numCols) {
      row(col) = bb.get() & 0xFF
    }
    row
  }

  def assertMagicNumber(expectedMagicNumber: Int, magicNumber: Int): Unit = {
    if (expectedMagicNumber != magicNumber) {
      expectedMagicNumber match {
        case LABEL_FILE_MAGIC_NUMBER =>
          throw new RuntimeException("This is not a label file")
        case IMAGE_FILE_MAGIC_NUMBER =>
          throw new RuntimeException("This is not a image file")
        case _ =>
          throw new RuntimeException(s"Expected magic number $expectedMagicNumber, found $magicNumber")
      }
    }
  }

  def loadFileToByteBuffer(inFile: String): java.nio.ByteBuffer = {
    ByteBuffer.wrap(loadFile(inFile))
  }

  def loadFile(inFile: String): Array[Byte] = {
    try {
      val f = new RandomAccessFile(inFile, "r")
      val chan = f.getChannel()
      val fileSize = chan.size()
      val bb = ByteBuffer.allocate(fileSize.asInstanceOf[Int])
      chan.read(bb)
      bb.flip()
      val baos = new ByteArrayOutputStream()
      for (i <- 0L until fileSize) {
        baos.write(bb.get())
      }
      chan.close()
      f.close()
      baos.toByteArray
    } catch {
      case e: Exception =>
        throw new RuntimeException(e);
    }
  }
}