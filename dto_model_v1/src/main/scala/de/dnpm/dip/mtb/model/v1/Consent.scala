package de.dnpm.dip.mtb.model.v1


import de.dnpm.dip.coding.{ 
  Coding,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}



final case class Consent
(
  id: Id[Consent],
  patient: Id[Patient],
  status: Consent.Status.Value
)

object Consent
{

  object Status
  extends CodedEnum("dnpm-dip/mtb/consent/status")
  with DefaultCodeSystem
  {
    val Active   = Value("active")
    val Rejected = Value("rejected")

    implicit val format: Format[Value] =
      Json.formatEnum(this)
  }


  implicit val format: OFormat[Consent] =
    Json.format[Consent]

}
