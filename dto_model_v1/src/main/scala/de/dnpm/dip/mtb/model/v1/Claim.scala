package de.dnpm.dip.mtb.model.v1



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
  Format,
  OFormat
}
import de.dnpm.dip.mtb.model.ClaimResponse.{ 
  Status,
  StatusReason
}


final case class Claim
(
  id: Id[Claim],
  patient: Id[Patient],
  therapy: Id[MTBMedicationRecommendation],
  issuedOn: LocalDate
)

object Claim
{
  implicit val format: OFormat[Claim] =
    Json.format[Claim]
}



final case class ClaimResponse
(
  id: Id[Claim],
  patient: Id[Patient],
  claim: Id[Claim],
  issuedOn: LocalDate,
  status: Status.Value,
  reason: Option[StatusReason.Value]
)

object ClaimResponse
{
  implicit val formatStatus: Format[Status.Value] =
    Json.formatEnum(Status)

  implicit val formatStatusReason: Format[StatusReason.Value] =
    Json.formatEnum(StatusReason)

  implicit val format: OFormat[ClaimResponse] =
    Json.format[ClaimResponse]
}

