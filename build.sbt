def removeSnapshot(str:     String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) = "com.github.Katrix-.KatLib" % s"katlib-$module" % "develop-SNAPSHOT" % Provided

lazy val publishResolver = {
  val artifactPattern = s"""${file("publish").absolutePath}/[revision]/[artifact]-[revision](-[classifier]).[ext]"""
  Resolver.file("publish").artifacts(artifactPattern)
}

lazy val commonSettings = Seq(
  name := s"ChitChat-${removeSnapshot(spongeApiVersion.value)}",
  organization := "net.katsstuff",
  version := "2.0.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  resolvers += "jitpack" at "https://jitpack.io",
  libraryDependencies += katLibDependecy("shared"),
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint", "-Yno-adapted-args", "-Ywarn-dead-code", "-Ywarn-unused-import"),
  crossPaths := false,
  assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("scala.**"     -> "io.github.katrix.katlib.shade.scala.@1").inAll,
    ShadeRule.rename("shapeless.**" -> "io.github.katrix.katlib.shade.shapeless.@1").inAll
  ),
  autoScalaLibrary := false,
  publishTo := Some(publishResolver),
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := false,
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.copy(`classifier` = Some("assembly"))
  },
  addArtifact(artifact in (Compile, assembly), assembly),
  spongePluginInfo := spongePluginInfo.value.copy(
    id = "chitchat",
    name = Some("ChitChat"),
    version = Some(s"${removeSnapshot(spongeApiVersion.value)}-${version.value}"),
    authors = Seq("Katrix"),
    dependencies = Set(
      DependencyInfo("spongeapi", Some(removeSnapshot(spongeApiVersion.value))),
      DependencyInfo("katlib", Some(s"${removeSnapshot(spongeApiVersion.value)}-2.0.1"))
    )
  )
)

lazy val chitChatShared = (project in file("shared"))
  .enablePlugins(SpongePlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "ChitChat-Shared",
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    assembleArtifact := false,
    spongeMetaCreate := false,
    //Default version, needs to build correctly against all supported versions
    spongeApiVersion := "4.1.0"
  )

lazy val chitChatV410 = (project in file("4.1.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(chitChatShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "4.1.0", libraryDependencies += katLibDependecy("4-1-0"))

lazy val chitChatV500 = (project in file("5.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(chitChatShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "5.0.0", libraryDependencies += katLibDependecy("5-0-0"))

lazy val chitChatV600 = (project in file("6.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(chitChatShared)
  .settings(commonSettings: _*)
  .settings(spongeApiVersion := "6.0.0-SNAPSHOT", libraryDependencies += katLibDependecy("6-0-0"))

lazy val chitChatRoot = (project in file("."))
  .settings(publishArtifact := false)
  .disablePlugins(AssemblyPlugin)
  .aggregate(chitChatShared, chitChatV410, chitChatV500, chitChatV600)
