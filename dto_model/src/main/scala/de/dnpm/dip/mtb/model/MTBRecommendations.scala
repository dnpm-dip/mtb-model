package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
  Period,
  Reference,
  Patient,
  TherapyRecommendation,
  MedicationRecommendation
}
import de.dnpm.dip.coding.atc.ATC
import play.api.libs.json.{
  Json,
  OFormat
}



final case class MTBMedicationRecommendation
(
  id: Id[MTBMedicationRecommendation],
  patient: Reference[Patient],
  indication: Reference[MTBDiagnosis],
  levelOfEvidence: Option[LevelOfEvidence],
  priority: Coding[TherapyRecommendation.Priority.Value],
  issuedOn: LocalDate,
  medication: Set[Coding[ATC]],
  supportingEvidence: List[Reference[_]]
)
extends MedicationRecommendation[ATC]


object MTBMedicationRecommendation
{
  implicit val format: OFormat[MTBMedicationRecommendation] =
    Json.format[MTBMedicationRecommendation]
}
