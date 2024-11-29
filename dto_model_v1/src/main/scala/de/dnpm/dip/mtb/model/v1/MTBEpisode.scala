package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Period,
  Patient
}
import play.api.libs.json.{
  Json, 
  OFormat
}


final case class MTBEpisode
(
  id: Id[MTBEpisode],
  patient: Id[Patient],
  period: Period[LocalDate],
)


object MTBEpisode
{
  implicit val format: OFormat[MTBEpisode] =
    Json.format[MTBEpisode]
}
