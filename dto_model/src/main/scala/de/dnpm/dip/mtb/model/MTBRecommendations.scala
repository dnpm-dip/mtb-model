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
  Medications,
  MedicationRecommendation,
  Study,
  StudyEnrollmentRecommendation
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
  indication: Option[Reference[MTBDiagnosis]],
  issuedOn: LocalDate,
  levelOfEvidence: Option[LevelOfEvidence],
  priority: Option[Coding[TherapyRecommendation.Priority.Value]],
  medication: Set[Coding[Medications]],
//  useType: Coding[MTBMedicationRecommendation.UseType.Value],
  supportingVariants: Option[List[Reference[Variant]]]
)
extends MedicationRecommendation[Medications]


object MTBMedicationRecommendation
{

  object UseType
  extends CodedEnum("dnpm-dip/mtb/recommendation/medication/use-type")
  with DefaultCodeSystem
  {
    val InLabel       = Value("in-label")
    val OffLabel      = Value("off-label")
    val Compassionate = Value("compassionate")

    override val display =
      Map(
        InLabel       -> "In-Label Use",
        OffLabel      -> "Off-Label Use",
        Compassionate -> "Compassionate Use",
      )

  }

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

  }

  implicit val format: OFormat[GeneticCounselingRecommendation] =
    Json.format[GeneticCounselingRecommendation]
}


final case class MTBStudyEnrollmentRecommendation
(
  id: Id[MTBStudyEnrollmentRecommendation],
  patient: Reference[Patient],
  reason: Reference[MTBDiagnosis],
  issuedOn: LocalDate,
  levelOfEvidence: Option[Coding[LevelOfEvidence.Grading.Value]],
  supportingVariants: Option[List[Reference[Variant]]],
  studies: Option[List[ExternalId[Study]]]
)
extends StudyEnrollmentRecommendation

object MTBStudyEnrollmentRecommendation
{
  implicit val format: OFormat[MTBStudyEnrollmentRecommendation] =
    Json.format[MTBStudyEnrollmentRecommendation]
}
