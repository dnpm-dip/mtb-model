package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Period,
  Reference,
  Patient,
  EpisodeOfCare,
}
import play.api.libs.json.{
  Json, 
  OFormat
}


final case class MTBEpisodeOfCare
(
  id: Id[MTBEpisodeOfCare],
  patient: Reference[Patient],
  period: Period[LocalDate],
  diagnoses: Option[List[Reference[MTBDiagnosis]]]
)
extends EpisodeOfCare


object MTBEpisodeOfCare
{
  implicit val format: OFormat[MTBEpisodeOfCare] =
    Json.format[MTBEpisodeOfCare]
}
