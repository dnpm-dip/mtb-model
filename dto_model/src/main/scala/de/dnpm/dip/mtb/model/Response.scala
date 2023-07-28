package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Patient,
  Reference
}
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem
}


object RECIST
extends CodedEnum("RECIST")
with DefaultCodeSystem
{
  val CR  = Value("CR")
  val PR  = Value("PR")
  val MR  = Value("MR")
  val SD  = Value("SD")
  val PD  = Value("PD")
  val NA  = Value("NA")
  val NYA = Value("NYA")

  override val display = {
    case CR  => "Complete Response"
    case PR  => "Partial Response"
    case MR  => "Mixed Response"
    case SD  => "Stable Disease"
    case PD  => "Progressive Disease"
    case NA  => "Not Assessable"
    case NYA => "Not Yet Assessable"
  }

/*  
  implicit val system =
    Coding.System[RECIST.Value]("RECIST")

  implicit val codingSystem: CodeSystem[RECIST.Value] =
    CodeSystem(
      Coding.System[RECIST.Value].uri,
      "RECIST",
      Some("RECIST"),
      CR  -> "Complete Response",
      PR  -> "Partial Response",
      MR  -> "Mixed Response",
      SD  -> "Stable Disease",
      PD  -> "Progressive Disease",
      NA  -> "Not Assessable",
      NYA -> "Not Yet Assessable"
    )
*/
}



final case class Response
(
  id: Id[Response],
  patient: Reference[Patient],
  therapy: Reference[MTBMedicationTherapy],
  effectiveDate: LocalDate,
  value: Coding[RECIST.Value]
)


