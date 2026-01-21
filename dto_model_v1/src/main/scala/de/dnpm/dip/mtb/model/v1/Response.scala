package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Patient,
}
import de.dnpm.dip.coding.Coding
import play.api.libs.json.{
  Json,
  JsObject,
  JsString,
  OFormat,
  Reads
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

  // Required to translate former RECIST values
  implicit def readsTherapyStatusReason(
    implicit reads: Reads[Coding[RECIST.Value]]
  ): Reads[Coding[RECIST.Value]] =
    reads.preprocess {
      case js: JsObject =>
        (js \ "code").as[String] match {
          case "NYA" => js + ("code" -> JsString("NA"))
          case _ => js
        }
    }

  implicit val format: OFormat[Response] =
    Json.format[Response]
}
