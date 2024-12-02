package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
  ExternalId,
  GeneAlterationReference,
  Reference,
  Patient,
  TherapyRecommendation,
  Medications,
  MedicationRecommendation,
  Study,
  StudyEnrollmentRecommendation
}
import play.api.libs.json.{
  Json,
  OFormat
}


final case class MTBMedicationRecommendation
(
  id: Id[MTBMedicationRecommendation],
  patient: Reference[Patient],
//  reason: Option[Reference[MTBDiagnosis]], //TODO
  indication: Option[Reference[MTBDiagnosis]],
  issuedOn: LocalDate,
  levelOfEvidence: Option[LevelOfEvidence],
  priority: Option[Coding[TherapyRecommendation.Priority.Value]],
  medication: Set[Coding[Medications]],
//  useType: Option[Coding[MTBMedicationRecommendation.UseType.Value]], //TODO
  supportingVariants: Option[List[GeneAlterationReference[Variant]]]
//  supportingVariants: Option[List[Reference[Variant]]]
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
    val SecPreventive = Value("sec-preventive")
    val Unknown       = Value("unknown")

    override val display =
      Map(
        InLabel       -> "In-Label Use",
        OffLabel      -> "Off-Label Use",
        Compassionate -> "Compassionate Use",
        SecPreventive -> "Sec-preventive",
        Unknown       -> "Unknown"
      )

  }

  implicit val format: OFormat[MTBMedicationRecommendation] =
    Json.format[MTBMedicationRecommendation]
}



final case class MTBProcedureRecommendation
(
  id: Id[MTBProcedureRecommendation],
  patient: Reference[Patient],
  indication: Option[Reference[MTBDiagnosis]],
  issuedOn: LocalDate,
  priority: Option[Coding[TherapyRecommendation.Priority.Value]],
  code: Coding[OncoProcedure.Type.Value],
//  supportingVariants: Option[List[Reference[Variant]]]
  supportingVariants: Option[List[GeneAlterationReference[Variant]]]
)
extends TherapyRecommendation



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
//  supportingVariants: Option[List[Reference[Variant]]],
  supportingVariants: Option[List[GeneAlterationReference[Variant]]],
  studies: Option[List[ExternalId[Study]]]
)
extends StudyEnrollmentRecommendation

object MTBStudyEnrollmentRecommendation
{
  implicit val format: OFormat[MTBStudyEnrollmentRecommendation] =
    Json.format[MTBStudyEnrollmentRecommendation]
}
