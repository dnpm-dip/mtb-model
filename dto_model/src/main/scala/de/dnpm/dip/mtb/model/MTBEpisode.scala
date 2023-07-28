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
  Episode
}


final case class MTBEpisode
(
  id: Id[MTBEpisode],
  patient: Reference[Patient],
  period: Period[LocalDate],
  status: Coding[Episode.Status.Value],
  diagnoses: List[Reference[MTBDiagnosis]]
)
extends Episode

