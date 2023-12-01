package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
  Period,
  Reference,
  Patient,
  CarePlan
}
import play.api.libs.json.{
  Json,
  OFormat
}


final case class MTBCarePlan
(
  id: Id[MTBCarePlan],
  patient: Reference[Patient],
  issuedOn: LocalDate,
  indication: Reference[MTBDiagnosis],
  statusReason: Option[Coding[MTBCarePlan.StatusReason.Value]],
  protocol: Option[String],
  medicationRecommendations: List[MTBMedicationRecommendation],
  //TODO: other recommendation types
)
extends CarePlan

object MTBCarePlan
{

  object StatusReason
  extends CodedEnum("mtb/careplan/status-reason")
  with DefaultCodeSystem
  {
    val NoTarget = Value("no-target")

    override val display = {
      case NoTarget => "Keine Therapeutische Konsequenz"
    }
  }

  implicit val format: OFormat[MTBCarePlan] =
    Json.format[MTBCarePlan]

}
