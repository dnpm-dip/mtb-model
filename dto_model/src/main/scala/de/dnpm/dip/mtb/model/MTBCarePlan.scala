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
  statusReason: Option[Coding[MTBCarePlan.StatusReason.Value]],
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
  type StatusReason = MTBCarePlan.StatusReason.type

  val therapyRecommendations = None
}


object MTBCarePlan
{

  object StatusReason
  extends CodedEnum("dnpm-dip/mtb/careplan/status-reason")
  with DefaultCodeSystem
  with CarePlan.NonInclusionReason
  {
    val NoTarget = Value("no-target")

    override val display =
      Map(
        NoTarget -> "Keine Therapeutische Konsequenz"
      )
      .orElse(
        defaultDisplay
      )
  }

  implicit val format: OFormat[MTBCarePlan] =
    Json.format[MTBCarePlan]

}
