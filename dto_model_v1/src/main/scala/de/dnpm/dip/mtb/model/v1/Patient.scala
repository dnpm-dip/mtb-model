package de.dnpm.dip.mtb.model.v1


import java.time.{
  LocalDate,
  YearMonth
}
import java.time.format.DateTimeFormatter
import scala.util.Try
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.{
  Address,
  Gender,
  Id,
  Organization
}
import play.api.libs.json.{
  Json,
  JsSuccess,
  JsError,
  JsString,
  Reads,
  Format,
  Writes,
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

  private val yyyyMM    = "yyyy-MM"
  private val yyyyMMFormatter = DateTimeFormatter.ofPattern(yyyyMM)

  implicit val formatYearMonth: Format[YearMonth] =
    Format(
      Reads(
        js =>
          for {
            s <- js.validate[String]
            result <-
              Try(
                YearMonth.parse(s,yyyyMMFormatter)
              )
              .orElse(
                Try(LocalDate.parse(s,DateTimeFormatter.ISO_LOCAL_DATE))
                  .map(d => YearMonth.of(d.getYear,d.getMonth))
              )
              .map(JsSuccess(_))
              .getOrElse(JsError(s"Invalid Year-Month value $s; expected format $yyyyMM (or $yyyyMM-DD as fallback)") )
          } yield result
      ),
      Writes(
        d => JsString(yyyyMMFormatter.format(d))
      )
    )


  implicit val reads: Reads[Patient] = 
    Json.reads[Patient]

  implicit val writes: OWrites[Patient] = 
    Json.writes[Patient]

}
