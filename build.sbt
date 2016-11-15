// Versions
lazy val scalaV = "2.11.8"
lazy val scalaDomV = "0.9.1"
lazy val scalajsjqueryV = "0.9.1"
lazy val scalajsReactV = "0.11.1"
lazy val scalajsHighchartsV = "1.1.2"
lazy val scalaCSSV = "0.4.1"
lazy val log4jsV = "1.4.10"
lazy val autowireV = "0.2.5"
lazy val prickleV = "1.1.12"
lazy val booPickleV = "1.2.4"
lazy val diodeV = "1.0.0"
lazy val uTestV = "0.4.3"

lazy val reactV = "15.1.0"
lazy val jQueryV = "2.2.4"
lazy val bootstrapV = "3.3.7"
lazy val highcharsV = "4.0.4"

lazy val fontawesomeV = "4.3.0-1"
lazy val scalajsScriptsV = "1.0.0"


// Server module
lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // compress CSS
  LessKeys.compress in Assets := true,
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
  libraryDependencies ++= Seq(specs2 % Test,
    //    "org.webjars" % "bootstrap" % "3.3.7-1" exclude("org.webjars", "jquery"),
    //    "org.webjars" % "jquery" % "2.2.4",
    "com.vmunier" %% "scalajs-scripts" % scalajsScriptsV,
    "org.webjars" % "font-awesome" % fontawesomeV % Provided,
    "org.webjars" % "bootstrap" % bootstrapV % Provided,
    "com.github.benhutchison" %% "prickle" % prickleV
  ),
  commands += ReleaseCmd
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  jsDependencies ++= Seq(
    "org.webjars" % "jquery" % jQueryV / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "bootstrap" % bootstrapV / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js",
    "org.webjars" % "highcharts" % highcharsV / "highcharts.js" dependsOn "jquery.js"
  ),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % scalaDomV,
    "be.doeraene" %%% "scalajs-jquery" % scalajsjqueryV,
    "com.github.benhutchison" %%% "prickle" % prickleV,
    "com.github.karasiq" %%% "scalajs-highcharts" % scalajsHighchartsV
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

lazy val ReleaseCmd = Command.command("release") {
  state => "set elideOptions in client := Seq(\"-Xelide-below\", \"WARNING\")" ::
    "client/clean" ::
    "client/test" ::
    "server/clean" ::
    "server/test" ::
    "server/dist" ::
    "set elideOptions in client := Seq()" ::
    state
}
