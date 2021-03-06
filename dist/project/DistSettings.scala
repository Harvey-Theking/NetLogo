import sbt._
import Keys.baseDirectory

import sbt.complete.Parser, Parser._

object DistSettings {
  def mapToParser[T](m: Map[String, T]): Parser[T] = {
    m.map(t => t._1 ^^^ t._2).reduceLeft(_ | _)
  }

  def mapToParserOpt[T](m: Map[String, T]): Option[Parser[T]] = {
    if (m.isEmpty)
      None
    else
      Some(m.map(t => t._1 ^^^ t._2).reduceLeft(_ | _))
  }

  lazy val aggregateJDKParser = settingKey[State => Parser[BuildJDK]]("parser for packageApp settings")

  lazy val buildDocs           = taskKey[Seq[File]]("render NetLogo documentation")
  // build application jar, resources
  lazy val buildNetLogo = taskKey[Unit]("build NetLogo")

  lazy val buildVariables = taskKey[Map[String, String]]("NetLogo template variables")

  lazy val modelCrossReference = taskKey[Unit]("add model cross references")

  lazy val netLogoRoot = settingKey[File]("Root directory of NetLogo project")

  lazy val netLogoVersion = settingKey[String]("Version of NetLogo under construction")

  lazy val netLogoLongVersion = settingKey[String]("Full version of NetLogo under construction")

  lazy val numericOnlyVersion = settingKey[String]("Version of NetLogo under construction (only numbers and periods)")

  lazy val packageAppParser = settingKey[State => Parser[(PlatformBuild, SubApplication, BuildJDK)]]("parser for packageApp settings")

  lazy val platformMap = settingKey[Map[String, PlatformBuild]]("map of names to platforms")

  lazy val subApplicationMap = settingKey[Map[String, SubApplication]]("map of names to sub-application")

  lazy val webTarget = settingKey[File]("location of finished website")

  lazy val aggregateOnlyFiles = taskKey[Seq[File]]("Files to be included in the aggregate root")

  lazy val jdkParser: Parser[BuildJDK] =
    (mapToParserOpt(JavaPackager.systemPackagerOptions.map(j => (j.version + "-" + j.arch -> j)).toMap)
      .map(p => (" " ~> p))
      .getOrElse(Parser.success(PathSpecifiedJDK)))

  lazy val settings = Seq(
    buildNetLogo := {
      def netLogoCmd(cmd: String): Unit = {
        RunProcess(Seq("./sbt", cmd), netLogoRoot.value, s"netlogo $cmd")
      }

      netLogoCmd("package")
      netLogoCmd("extensions")

      netLogoCmd("all-previews")
      netLogoCmd("test:run-main org.nlogo.tools.ModelResaver")
      modelCrossReference.value
      netLogoCmd("model-index")

      netLogoCmd("doc-smaller")
      buildDocs.value

      netLogoCmd("native-libs")

      RunProcess(Seq("./sbt", "package"), netLogoRoot.value / "Mathematica-Link", s"package mathematica link")
    },
    modelCrossReference := {
      ModelCrossReference(netLogoRoot.value)
    },
    buildDocs := {
      new NetLogoDocs(baseDirectory.value / "docs", netLogoRoot.value / "docs", netLogoRoot.value)
        .generate(buildVariables.value)
    },
    aggregateJDKParser := Def.toSParser(jdkParser)
  )
}
