package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
  Period,
  Patient,
  TherapyRecommendation,
}
import de.dnpm.dip.coding.atc.ATC
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}
import de.dnpm.dip.mtb.model.LevelOfEvidence



final case class MTBMedicationRecommendation
(
  id: Id[MTBMedicationRecommendation],
  patient: Id[Patient],
  diagnosis: Id[MTBDiagnosis],
  levelOfEvidence: Option[LevelOfEvidence],
  priority: Option[TherapyRecommendation.Priority.Value],
  issuedOn: Option[LocalDate],
  medication: Option[Set[Coding[ATC]]],
  ngsReport: Option[Id[SomaticNGSReport]],
  supportingVariants: Option[List[Id[Variant]]]
)


object MTBMedicationRecommendation
{
  implicit val formatPriority: Format[TherapyRecommendation.Priority.Value] =
    Json.formatEnum(TherapyRecommendation.Priority)

  implicit val format: OFormat[MTBMedicationRecommendation] =
    Json.format[MTBMedicationRecommendation]
}



final case class GeneticCounselingRecommendation
(
  id: Id[GeneticCounselingRecommendation],
  patient: Id[Patient],
  issuedOn: Option[LocalDate],
  reason: String
)

object GeneticCounselingRecommendation
{

  implicit val format: OFormat[GeneticCounselingRecommendation] =
    Json.format[GeneticCounselingRecommendation]
}


final case class StudyEnrollmentRecommendation
(
  id: Id[StudyEnrollmentRecommendation],
  patient: Id[Patient],
  reason: Id[MTBDiagnosis],
  issuedOn: Option[LocalDate],
  nctNumber: String
)

object StudyEnrollmentRecommendation
{

  import de.dnpm.dip.util.json._

  implicit val format: OFormat[StudyEnrollmentRecommendation] =
    Json.format[StudyEnrollmentRecommendation]
}
