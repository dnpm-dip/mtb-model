package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
  Medications,
  Period,
  Reference,
  Patient,
  Therapy,
  TherapyRecommendation,
  Procedure,
  SystemicTherapy
}
import play.api.libs.json.{
  Json,
  OFormat
}


object MTBTherapy
{

  object Intent
  extends CodedEnum("dnpm-dip/therapy/intent")
  with DefaultCodeSystem
  {
    val K, P, S, X = Value

    override val display =
      Map(
        K -> "Kurativ",
        P -> "Palliativ",
        S -> "Sonstiges",
        X -> "Keine Angabe",
      )
  }


  object StatusReason
  extends CodedEnum("dnpm-dip/therapy/status-reason")
  with DefaultCodeSystem
  {

    val PaymentRefused                       = Value("payment-refused")
    val PaymentPending                       = Value("payment-pending")
    val PaymentEnded                         = Value("payment-ended")
    val NoIndication                         = Value("no-indication")
    val MedicalReasons                       = Value("medical-reasons")
    val PatientRefusal                       = Value("patient-refusal")
    val PatientWish                          = Value("patient-wish")
    val PatientDeath                         = Value("patient-death")
    val LostToFU                             = Value("lost-to-fu")
    val Remission                            = Value("chronic-remission")
    val Progression                          = Value("progression")
    val Toxicity                             = Value("toxicity")
    val OtherTherapyChosen                   = Value("other-therapy-chosen")
    val Deterioration                        = Value("deterioration")
    val BestSupportiveCare                   = Value("best-supportive-care")
    val RegularCompletion                    = Value("regular-completion")
    val RegularCompletionWithDosageReduction = Value("regular-completion-with-dosage-reduction")
    val RegularCompletionWithSubstanceChange = Value("regular-completion-with-substance-change")
    val Other                                = Value("other")

    override val display =
      Map(
        PaymentRefused                       -> "Kostenübernahme abgelehnt",
        PaymentPending                       -> "Kostenübernahme noch ausstehend",
        PaymentEnded                         -> "Ende der Kostenübernahme",
        NoIndication                         -> "Klinisch keine Indikation",
        MedicalReasons                       -> "Medizinische Gründe",
        PatientRefusal                       -> "Therapie durch Patient abgelehnt",
        PatientWish                          -> "Auf Wunsch des Patienten",
        PatientDeath                         -> "Tod",
        LostToFU                             -> "Lost to follow-up",
        Remission                            -> "Anhaltende Remission",
        Progression                          -> "Progression",
        Toxicity                             -> "Toxizität",
        OtherTherapyChosen                   -> "Wahl einer anderen Therapie durch Behandler",
        Deterioration                        -> "Zustandsverschlechterung",
        BestSupportiveCare                   -> "Best Supportive Care",
        RegularCompletion                    -> "Reguläres Ende",
        RegularCompletionWithDosageReduction -> "Reguläres Ende mit Dosisreduktion",
        RegularCompletionWithSubstanceChange -> "Reguläres Ende mit Substanzwechsel",
        Other                                -> "Weitere Gründe"
      )
  }

}


final case class MTBSystemicTherapy
(
  id: Id[MTBSystemicTherapy],
  patient: Reference[Patient],
  reason: Option[Reference[MTBDiagnosis]],
  therapyLine: Option[Int],
  intent: Option[Coding[MTBTherapy.Intent.Value]],
  category: Option[Coding[MTBSystemicTherapy.Category.Value]],
  basedOn: Option[Reference[MTBMedicationRecommendation]],
  recordedOn: LocalDate,
  status: Coding[Therapy.Status.Value],
  statusReason: Option[Coding[MTBTherapy.StatusReason.Value]],
  recommendationFulfillmentStatus: Option[Coding[MTBSystemicTherapy.RecommendationFulfillmentStatus.Value]],
  period: Option[Period[LocalDate]],
  medication: Option[Set[Coding[Medications]]],
  notes: Option[List[String]]
)
extends SystemicTherapy[Medications]


object MTBSystemicTherapy
{

  object Category
  extends CodedEnum("dnpm-dip/therapy/category")
  with DefaultCodeSystem
  {
    val A, I, O, N, S = Value

    override val display =
      Map(
        A -> "Adjuvant",
        I -> "Intraopterativ",
        O -> "Ohne Bezug zur operativen Therapie",
        N -> "Neoadjuvant",
        S -> "Sonstiges",
      )
  }

  object DosageDensity
  extends CodedEnum("dnpm-dip/therapy/dosage-density")
  with DefaultCodeSystem
  {
    val Under50 = Value("under-50%")
    val Over50  = Value("over-50%")

    override val display =
      Map(
        Under50 -> "< 50 %",
        Over50  -> ">= 50 %",
      )
  }

  object RecommendationFulfillmentStatus
  extends CodedEnum("dnpm-dip/therapy/recommendation-fulfillment-status")
  with DefaultCodeSystem
  {
    val Partial  = Value("partial")
    val Complete = Value("complete")

    override val display =
      Map(
        Partial  -> "Partiell",
        Complete -> "Komplett",
      )
  }

  implicit val format: OFormat[MTBSystemicTherapy] =
    Json.format[MTBSystemicTherapy]
}



final case class OncoProcedure
(
  id: Id[OncoProcedure],
  patient: Reference[Patient],
  reason: Option[Reference[MTBDiagnosis]],
  therapyLine: Option[Int],
  intent: Option[Coding[MTBTherapy.Intent.Value]],
  basedOn: Option[Reference[TherapyRecommendation]],
  code: Coding[OncoProcedure.Type.Value],
  status: Coding[Therapy.Status.Value],
  statusReason: Option[Coding[MTBTherapy.StatusReason.Value]],
  recordedOn: LocalDate,
  period: Option[Period[LocalDate]],
  notes: Option[List[String]]
)
extends Procedure
{
  val category = None
}

object OncoProcedure
{

  object Type
  extends CodedEnum("dnpm-dip/mtb/procedure/type")
  with DefaultCodeSystem
  {
    val Surgery         = Value("surgery")
    val RadioTherapy    = Value("radio-therapy")
    val NuclearMedicine = Value("nuclear-medicine")

    override val display = {
      case Surgery         => "OP"
      case RadioTherapy    => "Strahlen-Therapie"
      case NuclearMedicine => "Nuklearmedizinische Therapie"
    }
  }


  implicit val format: OFormat[OncoProcedure] =
    Json.format[OncoProcedure]

}
