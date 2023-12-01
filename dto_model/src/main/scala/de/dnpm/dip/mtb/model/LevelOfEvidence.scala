package de.dnpm.dip.mtb.model


import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem,
}
import play.api.libs.json.{
  Json,
  OFormat
}

final case class LevelOfEvidence
(
  grading: Coding[LevelOfEvidence.Grading.Value],
  addendums: Option[Set[Coding[LevelOfEvidence.Addendum.Value]]]
)

object LevelOfEvidence
{

  object Grading
  extends CodedEnum("mtb/level-of-evidence/grading")
  with DefaultCodeSystem
  {
    val Undefined = Value("undefined")
    val m1A,m1B,m1C,m2A,m2B,m2C,m3,m4 = Value

    override val display = {
      case Undefined => "N/A"
      case g         => g.toString
    }

  }

  object Addendum
  extends CodedEnum("mtb/level-of-evidence/addendum")
  with DefaultCodeSystem
  {
    val IS = Value("is")
    val IV = Value("iv")
    val Z  = Value("Z")
    val R  = Value("R")
  }

  implicit val format: OFormat[LevelOfEvidence] =
    Json.format[LevelOfEvidence]

}
