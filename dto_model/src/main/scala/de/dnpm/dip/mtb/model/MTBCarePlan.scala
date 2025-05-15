package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
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
  reason: Option[Reference[MTBDiagnosis]],
  issuedOn: LocalDate,
  noSequencingPerformedReason: Option[Coding[CarePlan.NoSequencingPerformedReason.Value]],
  recommendationsMissingReason: Option[Coding[MTBCarePlan.RecommendationsMissingReason.Value]],
  geneticCounselingRecommendation: Option[GeneticCounselingRecommendation],
  medicationRecommendations: Option[List[MTBMedicationRecommendation]],
  procedureRecommendations: Option[List[MTBProcedureRecommendation]],
  studyEnrollmentRecommendations: Option[List[MTBStudyEnrollmentRecommendation]],
  histologyReevaluationRequests: Option[List[HistologyReevaluationRequest]],
  rebiopsyRequests: Option[List[RebiopsyRequest]],
  notes: Option[List[String]]
)
extends CarePlan
{
  override val therapyRecommendations = None
}


object MTBCarePlan
{

  object RecommendationsMissingReason
  extends CodedEnum("dnpm-dip/mtb/careplan/recommendations-missing-reason")
  with DefaultCodeSystem
  {
    val NoTarget = Value("no-target")

    override val display =
      Map(
        NoTarget -> "Keine Therapeutische Konsequenz"
      )
  }

  implicit val format: OFormat[MTBCarePlan] =
    Json.format[MTBCarePlan]

}
