package de.dnpm.dip.mtb.model


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
  ExternalId,
  Period,
  Reference,
  Patient,
  TherapyRecommendation,
  MedicationRecommendation
}
import de.dnpm.dip.coding.atc.ATC
import play.api.libs.json.{
  Json,
  Format,
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
  supportingEvidence: Option[List[Reference[Variant]]]
)
extends MedicationRecommendation[ATC]


object MTBMedicationRecommendation
{
  implicit val format: OFormat[MTBMedicationRecommendation] =
    Json.format[MTBMedicationRecommendation]
}



final case class GeneticCounselingRecommendation
(
  id: Id[GeneticCounselingRecommendation],
  patient: Reference[Patient],
  issuedOn: LocalDate,
  reason: Coding[GeneticCounselingRecommendation.Reason.Value]
)

object GeneticCounselingRecommendation
{

  object Reason
  extends CodedEnum("dnpm-dip/mtb/recommendation/genetic-counseling/reason")
  with DefaultCodeSystem
  {
    val FamilyAnamnesis = Value("family-anamnesis")
    val SelfAnamnesis   = Value("self-anamnesis")
    val SecondaryTumor  = Value("secondary-tumor")
    val Other	        = Value("other")  
    val Unknown         = Value("unknown")

    override val display =
      Map(
        FamilyAnamnesis -> "Familienanamnese",
        SelfAnamnesis   -> "Eigenanamnese",
        SecondaryTumor  -> "Zweittumor",
        Other	        -> "Andere",
        Unknown         -> "Unbekannt"
      )

    implicit val format: Format[Value] =
      Json.formatEnum(this)
  }

  implicit val format: OFormat[GeneticCounselingRecommendation] =
    Json.format[GeneticCounselingRecommendation]
}


sealed trait Study

final case class StudyEnrollmentRecommendation
(
  id: Id[StudyEnrollmentRecommendation],
  patient: Reference[Patient],
  reason: Reference[MTBDiagnosis],
  issuedOn: LocalDate,
  levelOfEvidence: Option[Coding[LevelOfEvidence.Grading.Value]],
  supportingEvidence: List[Reference[Variant]],
  studyIds: NonEmptyList[ExternalId[Study]]
)

object StudyEnrollmentRecommendation
{

  import de.dnpm.dip.util.json._

  implicit val format: OFormat[StudyEnrollmentRecommendation] =
    Json.format[StudyEnrollmentRecommendation]
}
