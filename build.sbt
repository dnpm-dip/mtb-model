
/*
 build.sbt adapted from https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt
*/


name := "mtb-model"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.16"
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
     generators,
     dto_model_v1,
  )


lazy val dto_model = project
  .settings(
    name := "mtb-dto-model",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.core,
      dependencies.scala_jsonschema % Test,
      dependencies.scala_jsonschema_cats % Test, 
      dependencies.scala_jsonschema_play % Test, 
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
      dependencies.atc_catalogs,
      dependencies.hgnc_geneset,
      dependencies.json_schema_validator
    )
  )
  .dependsOn(
    dto_model
  )


lazy val dto_model_v1 = project
  .settings(
    name := "mtb-dto-model-v1",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.bwhc_mtb_dtos,
      dependencies.bwhc_dto_gens,
      dependencies.hgnc_geneset
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
    val scalatest             = "org.scalatest"       %% "scalatest"                  % "3.1.1"        % Test
    val generators            = "de.ekut.tbi"         %% "generators"                 % "1.0-SNAPSHOT"
    val core                  = "de.dnpm.dip"         %% "core"                       % "1.0-SNAPSHOT"
    val icd10gm               = "de.dnpm.dip"         %% "icd10gm-impl"               % "1.0-SNAPSHOT" % Test
    val icdo3                 = "de.dnpm.dip"         %% "icdo3-impl"                 % "1.0-SNAPSHOT" % Test
    val icd_catalogs          = "de.dnpm.dip"         %% "icd-claml-packaged"         % "1.0-SNAPSHOT" % Test
    val atc_impl              = "de.dnpm.dip"         %% "atc-impl"                   % "1.0-SNAPSHOT" % Test
    val atc_catalogs          = "de.dnpm.dip"         %% "atc-catalogs-packaged"      % "1.0-SNAPSHOT" % Test
    val hgnc_geneset          = "de.dnpm.dip"         %% "hgnc-gene-set-impl"         % "1.0-SNAPSHOT" % Test
    val bwhc_mtb_dtos         = "de.bwhc"             %% "mtb-dtos"                   % "1.0"          % Test
    val bwhc_dto_gens         = "de.bwhc"             %% "mtb-dto-generators"         % "1.0"          % Test
    val scala_jsonschema      = "com.github.andyglow" %% "scala-jsonschema"           % "0.7.11"
    val scala_jsonschema_cats = "com.github.andyglow" %% "scala-jsonschema-cats"      % "0.7.11"
    val scala_jsonschema_play = "com.github.andyglow" %% "scala-jsonschema-play-json" % "0.7.11"
    val json_schema_validator = "com.networknt"       % "json-schema-validator"       % "1.5.5"        % Test
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings


// Compiler options from: https://alexn.org/blog/2020/05/26/scala-fatal-warnings/
lazy val compilerOptions = Seq(
  // Feature options
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ymacro-annotations",

  // Warnings as errors!
  "-Xfatal-warnings",

  // Linting options
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-Wvalue-discard",

  // Deactivated to avoid many false positives from 'evidence' parameters in context bounds
//  "-Wunused:implicits",
//  "-Wunused:params",
)


lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)

