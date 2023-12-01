
/*
 build.sbt adapted from https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt
*/


name := "mtb-model"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "1.0-SNAPSHOT"


//-----------------------------------------------------------------------------
// PROJECTS
//-----------------------------------------------------------------------------

lazy val global = project
  .in(file("."))
  .settings(
    settings,
    publish / skip := true
  )
  .aggregate(
     dto_model,
     generators
  )


lazy val dto_model = project
  .settings(
    name := "mtb-dto-model",
    settings,
    libraryDependencies ++= Seq(
      dependencies.core,
    )
  )


lazy val generators = project
  .settings(
    name := "mtb-dto-generators",
    settings,
    libraryDependencies ++= Seq(
      dependencies.generators,
      dependencies.scalatest,
      dependencies.icd10gm,
      dependencies.icdo3,
      dependencies.icd_catalogs,
      dependencies.atc_impl,
      dependencies.atc_catalogs
    )
  )
  .dependsOn(
    dto_model
  )



//-----------------------------------------------------------------------------
// DEPENDENCIES
//-----------------------------------------------------------------------------

lazy val dependencies =
  new {
    val scalatest    = "org.scalatest"  %% "scalatest"              % "3.1.1"        % Test
    val generators   = "de.ekut.tbi"    %% "generators"             % "1.0-SNAPSHOT"
    val core         = "de.dnpm.dip"    %% "core"                   % "1.0-SNAPSHOT"
    val icd10gm      = "de.dnpm.dip"    %% "icd10gm-impl"           % "1.0-SNAPSHOT" % Test
    val icdo3        = "de.dnpm.dip"    %% "icdo3-impl"             % "1.0-SNAPSHOT" % Test
    val icd_catalogs = "de.dnpm.dip"    %% "icd-claml-packaged"     % "1.0-SNAPSHOT" % Test
    val atc_impl     = "de.dnpm.dip"    %% "atc-impl"               % "1.0-SNAPSHOT" % Test
    val atc_catalogs = "de.dnpm.dip"    %% "atc-catalogs-packaged"  % "1.0-SNAPSHOT" % Test
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings


lazy val compilerOptions = Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-feature",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-deprecation",
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)

