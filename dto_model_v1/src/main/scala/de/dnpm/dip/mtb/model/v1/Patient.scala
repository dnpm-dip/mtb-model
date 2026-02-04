package de.dnpm.dip.mtb.model.v1


import java.time.YearMonth
import de.dnpm.dip.model.{
  Address,
  Gender,
  Id,
  Organization
}
import play.api.libs.json.{
  Json,
  Reads,
  OWrites
}


final case class Patient
(
  id: Id[Patient],
  gender: Gender.Value,
  birthDate: YearMonth,
  dateOfDeath: Option[YearMonth],
  insurance: Option[Id[Organization]],
  address: Option[Address]
)

object Patient
{

  import de.dnpm.dip.util.json.{ 
    readsYearMonth,
    writesYearMonth
  }

  implicit val reads: Reads[Patient] = 
    Json.reads[Patient]

  implicit val writes: OWrites[Patient] = 
    Json.writes[Patient]

}
