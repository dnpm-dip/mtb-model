package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Patient
}
import de.dnpm.dip.coding.Coding
import play.api.libs.json.{
  Json,
  OFormat
}
import de.dnpm.dip.mtb.model.ECOG


final case class PerformanceStatus
(
  id: Id[PerformanceStatus],
  patient: Id[Patient],
  effectiveDate: LocalDate,
  value: Coding[ECOG.Value]
)


object PerformanceStatus
{
  implicit val format: OFormat[PerformanceStatus] =
    Json.format[PerformanceStatus]
}
