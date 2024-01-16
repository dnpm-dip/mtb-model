package de.dnpm.dip.mtb.model



import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem,
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.model.{
  Id,
  Period,
  Reference,
  Patient,
  Therapy,
  MedicationTherapy
}
import play.api.libs.json.{
  Json,
  OFormat
}



final case class Claim
(
  id: Id[Claim],
  patient: Reference[Patient],
  recommendation: Reference[MTBMedicationRecommendation],
//  requestedMedication: Option[Set[Coding[ATC]]]
  issuedOn: LocalDate,
  status: Coding[Claim.Status.Value],
)

object Claim
{

  object Status
  extends CodedEnum("dnpm-dip/mtb/claim/status")
  with DefaultCodeSystem
  {

    val InitialClaim  = Value("initial-claim")
    val Revocation    = Value("revocation")
    val FollowUpClaim = Value("follow-up-claim")
    val Unknown       = Value("unknown")

    override val display =
      Map(
        InitialClaim  -> "Erstantrag",
        Revocation    -> "Widerspruch",
        FollowUpClaim -> "Folgeantrag",
        Unknown       -> "Unbekant"
      )
  }


  implicit val format: OFormat[Claim] =
    Json.format[Claim]
}





final case class ClaimResponse
(
  id: Id[Claim],
  patient: Reference[Patient],
  claim: Reference[Claim],
  issuedOn: LocalDate,
  status: Coding[ClaimResponse.Status.Value],
  statusReason: Option[Coding[ClaimResponse.StatusReason.Value]]
)

object ClaimResponse
{

  object Status
  extends CodedEnum("dnpm-dip/mtb/claim-response/status")
  with DefaultCodeSystem
  {

    val Accepted = Value("accepted")
    val Rejected = Value("rejected")
    val Unknown  = Value("unknown")

    override val display =
      Map(
        Accepted -> "Angenommen",
        Rejected -> "Abgelehnt",
        Unknown  -> "Unbekant"
      )
  }

  object StatusReason
  extends CodedEnum("dnpm-dip/mtb/claim-response/status-reason")
  with DefaultCodeSystem
  {

    //TODO:
    // Standardtherapie nicht ausgeschöpft (Neuantrag erforderlich)
    // Standardtherapie nicht ausgeschöpft (kein Neuantrag erforderlich)


    val InsufficientEvidence    = Value("insufficient-evidence")
    val FormalReasons           = Value("formal-reasons")
    val OtherTherapyRecommended = Value("other-therapy-recommended")
    val InclusionInStudy        = Value("inclusion-in-study")
    val ApprovalRevocation      = Value("approval-revocation")
    val Other                   = Value("other")
    val Unknown                 = Value("unknown")

    override val display =
      Map(
        InsufficientEvidence     -> "Nicht ausreichende Evidenz",
        FormalReasons            -> "Inhaltliche Gründe",
        OtherTherapyRecommended  -> "Andere Therapie vorgeschlagen",
        InclusionInStudy         -> "Studieneinschluss",
        ApprovalRevocation       -> "Rücknahme der Zulassung",
        Other                    -> "Weitere Gründe",
        Unknown                  -> "Unbekant"
      )
  }



  implicit val format: OFormat[ClaimResponse] =
    Json.format[ClaimResponse]
}

