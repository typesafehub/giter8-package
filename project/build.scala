import sbt._
import Keys._

object GiterateBuild extends Build {
  // This build creates a SBT plugin with handy features *and* bundles the SBT script for distribution.
  val root = Project("giter8-package", file(".")) settings(Packaging.settings:_*) settings(version := "0.5.3")
}
