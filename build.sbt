
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
      dependencies.generators
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
    val scalatest   = "org.scalatest"  %% "scalatest"   % "3.1.1" % Test
    val core        = "de.dnpm.dip"    %% "core"        % "1.0-SNAPSHOT"
    val generators  = "de.ekut.tbi"    %% "generators"  % "0.1-SNAPSHOT"
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

