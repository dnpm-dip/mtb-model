package de.dnpm.dip.mtb.model


import java.time.LocalDate
import cats.Applicative
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI
}
import de.dnpm.dip.model.{
  Id,
  GeneAlterationReference,
  Reference,
  ExternalReference,
  Patient,
  Recommendation,
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
import shapeless.{
  Coproduct,
  CNil,
  :+:
}
import shapeless.ops.coproduct.Inject


object MTBRecommendation
{

  object Priority
  extends CodedEnum("dnpm-dip/mtb/recommendation/priority")
  with DefaultCodeSystem
  {
    val One    = Value("1")
    val Two    = Value("2")
    val Three  = Value("3")
    val Four   = Value("4")
    val Five   = Value("5")
    val Six    = Value("6")
    val Seven  = Value("7")
    val Eight  = Value("8")
    val Nine   = Value("9")
    val Ten    = Value("10")
    val Eleven = Value("11")
    val Twelve = Value("12")

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }
  }


  type SupportingFinding = BRCAness :+: HRDScore :+: MSI :+: TMB :+: CNil

  object SupportingFinding
  {
    def apply[T](t: T)(implicit inject: Inject[SupportingFinding,T]): SupportingFinding =
      Coproduct[SupportingFinding](t)
  }

}

trait MTBRecommendation
{
  this: Recommendation =>

  val levelOfEvidence: Option[LevelOfEvidence]
  val supportingFindings: Option[List[Reference[MTBRecommendation.SupportingFinding]]]
}


final case class MTBMedicationRecommendation
(
  id: Id[MTBMedicationRecommendation],
  patient: Reference[Patient],
  reason: Option[Reference[MTBDiagnosis]],
  issuedOn: LocalDate,
  priority: Coding[MTBRecommendation.Priority.Value],
  levelOfEvidence: Option[LevelOfEvidence],
  category: Option[Set[Coding[MTBMedicationRecommendation.Category.Value]]],
  medication: Set[Coding[Medications]],
  useType: Option[Coding[MTBMedicationRecommendation.UseType.Value]],
  supportingVariants: Option[List[GeneAlterationReference[Variant]]],
  supportingFindings: Option[List[Reference[MTBRecommendation.SupportingFinding]]]
)
extends MTBRecommendation
with MedicationRecommendation[Medications]


object MTBMedicationRecommendation
{

  object Category
  extends CodedEnum("dnpm-dip/mtb/recommendation/systemic-therapy/category")
  with DefaultCodeSystem
  {

    val CH, HO, IM, ZS, SZ, SO = Value

    override val display =
      Map(
        CH -> "Chemotherapie",
        HO -> "Hormontherapie",
        IM -> "Immun-/Antikörpertherapie",
        ZS -> "zielgerichtete Substanzen",
        SZ -> "Stammzelltransplantation (inklusive Knochenmarktransplantation)",
        SO -> "Sonstiges"
      )

  }


  object UseType
  extends CodedEnum("dnpm-dip/mtb/recommendation/systemic-therapy/use-type")
  with DefaultCodeSystem
  {
    val InLabel       = Value("in-label")
    val OffLabel      = Value("off-label")
    val Compassionate = Value("compassionate")
    val SecPreventive = Value("sec-preventive")
    val Unknown       = Value("unknown")

    override val display =
      Map(
        InLabel       -> "In-label Use",
        OffLabel      -> "Off-label Use",
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
  reason: Option[Reference[MTBDiagnosis]],
  issuedOn: LocalDate,
  priority: Coding[MTBRecommendation.Priority.Value],
  levelOfEvidence: Option[LevelOfEvidence],
  code: Coding[MTBProcedureRecommendation.Category.Value],
  supportingVariants: Option[List[GeneAlterationReference[Variant]]],
  supportingFindings: Option[List[Reference[MTBRecommendation.SupportingFinding]]]
)
extends MTBRecommendation
with TherapyRecommendation

object MTBProcedureRecommendation
{

  object Category
  extends CodedEnum("dnpm-dip/mtb/recommendation/procedure/category")
  with DefaultCodeSystem
  {

    val WW, AS, WS, OP, ST, SO = Value

    override val display =
      Map(
        WW -> "Watchful Waiting",
        AS -> "Active Surveillance",
        WS -> "Wait and see",
        OP -> "Operation",
        ST -> "Strahlentherapie",
        SO -> "Sonstiges"
      )

  }

  implicit val format: OFormat[MTBProcedureRecommendation] =
    Json.format[MTBProcedureRecommendation]
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
  reason: Option[Reference[MTBDiagnosis]],
  issuedOn: LocalDate,
  levelOfEvidence: Option[LevelOfEvidence],
  priority: Coding[MTBRecommendation.Priority.Value],
  study: NonEmptyList[ExternalReference[Study,Study.Registries]],
  medication: Option[Set[Coding[Medications]]],
  supportingVariants: Option[List[GeneAlterationReference[Variant]]],
  supportingFindings: Option[List[Reference[MTBRecommendation.SupportingFinding]]]
)
extends StudyEnrollmentRecommendation
with MTBRecommendation

object MTBStudyEnrollmentRecommendation
{

  // For Reads/Writes of NonEmptyList
  import de.dnpm.dip.util.json.{
    readsNel,
    writesNel
  }

  implicit val format: OFormat[MTBStudyEnrollmentRecommendation] =
    Json.format[MTBStudyEnrollmentRecommendation]
}


final case class HistologyReevaluationRequest
(
  id: Id[HistologyReevaluationRequest],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  issuedOn: LocalDate
)

object HistologyReevaluationRequest
{
  implicit val format: OFormat[HistologyReevaluationRequest] =
    Json.format[HistologyReevaluationRequest]
}


final case class RebiopsyRequest
(
  id: Id[RebiopsyRequest],
  patient: Reference[Patient],
  tumorEntity: Reference[MTBDiagnosis],
  issuedOn: LocalDate
)

object RebiopsyRequest
{
  implicit val format: OFormat[RebiopsyRequest] =
    Json.format[RebiopsyRequest]
}
