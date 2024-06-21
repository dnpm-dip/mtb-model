package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem
}
import de.dnpm.dip.model.{
  Id,
  Period,
  Reference,
  Patient,
  EpisodeOfCare,
  TransferTAN
}
import play.api.libs.json.{
  Json, 
  OFormat
}


final case class MTBEpisodeOfCare
(
  id: Id[MTBEpisodeOfCare],
  transferTan: Option[Id[TransferTAN]],
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
