package de.dnpm.dip.mtb.model.v1


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
  patient: Id[Patient],
  diagnosis: Id[MTBDiagnosis],
  issuedOn: Option[LocalDate],
  noTargetFinding: Option[MTBCarePlan.NoTargetFinding],
  description: Option[String],
  recommendations: Option[List[Id[MTBMedicationRecommendation]]],
  geneticCounsellingRequest: Option[Id[GeneticCounselingRecommendation]],
//  rebiopsyRequests: Option[List[RebiopsyRequest.Id]]
  studyInclusionRequests: Option[List[Id[StudyEnrollmentRecommendation]]]
)


object MTBCarePlan
{

  case class NoTargetFinding
  (
    patient: Id[Patient],
    diagnosis: Id[MTBDiagnosis],
    issuedOn: Option[LocalDate]
  )


  implicit val formatNoTargetFinding: OFormat[NoTargetFinding] =
    Json.format[NoTargetFinding]

  implicit val format: OFormat[MTBCarePlan] =
    Json.format[MTBCarePlan]

}
