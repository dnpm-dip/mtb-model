package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Patient,
}
import de.dnpm.dip.coding.Coding
import play.api.libs.json.{
  Json,
  OFormat
}
import de.dnpm.dip.mtb.model.RECIST


final case class Response
(
  id: Id[Response],
  patient: Id[Patient],
  therapy: Id[MTBMedicationTherapy],
  effectiveDate: LocalDate,
  value: Coding[RECIST.Value]
)

object Response
{
  implicit val format: OFormat[Response] =
    Json.format[Response]
}
