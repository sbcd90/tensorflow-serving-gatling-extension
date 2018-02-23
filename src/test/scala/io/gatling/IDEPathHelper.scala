package io.gatling

import java.nio.file.Path
import io.gatling.commons.util.PathHelper._

object IDEPathHelper {

  val gatlingConfUrl: Path = getClass.getClassLoader.getResource("gatling.conf").toURI
  val projectRootDir = gatlingConfUrl.ancestor(3)

  val mavenSourcesDirectory = projectRootDir / "src" / "main" / "scala"
  val mavenResourcesDirectory = projectRootDir / "src" / "test" / "resources"
  val mavenTargetDirectory = projectRootDir / "target"
  val mavenBinariesDirectory = projectRootDir / "test-classes"

  val dataDirectory = projectRootDir / "data"
  val bodiesDirectory = projectRootDir / "bodies"

  val resultsDirectory = projectRootDir / "io/gatling"
}