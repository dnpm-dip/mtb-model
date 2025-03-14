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
  medicationRecommendations: Option[List[MTBMedicationRecommendation]],
  geneticCounselingRecommendation: Option[GeneticCounselingRecommendation],
  studyEnrollmentRecommendations: Option[List[MTBStudyEnrollmentRecommendation]],
//  studyEnrollmentRecommendation: Option[MTBStudyEnrollmentRecommendation],
  notes: Option[List[String]]
)
extends CarePlan
{
  val therapyRecommendations = None
}


object MTBCarePlan
{

  object StatusReason
  extends CodedEnum("dnpm-dip/mtb/careplan/status-reason")
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
