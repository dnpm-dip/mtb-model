package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.{
  Id,
  Period,
  Patient,
  Therapy
}
import de.dnpm.dip.mtb.model.MTBTherapy
import play.api.libs.json.{
  Json,
  JsObject,
  JsString,
  Format,
  Reads,
  OFormat
}


final case class MTBMedicationTherapy
(
  id: Id[MTBMedicationTherapy],
  patient: Id[Patient],
  diagnosis: Option[Id[MTBDiagnosis]],
  therapyLine: Option[Int],
  basedOn: Option[Id[MTBMedicationRecommendation]],
  recordedOn: Option[LocalDate],
  status: Option[Therapy.Status.Value],
  period: Option[Period[LocalDate]],
  medication: Option[Set[Coding[Any]]],
  notDoneReason: Option[Coding[MTBTherapy.StatusReason.Value]],
  reasonStopped: Option[Coding[MTBTherapy.StatusReason.Value]],
  note: Option[String],
)


object MTBMedicationTherapy
{

  implicit val formatTherapyStatus: Format[Therapy.Status.Value] =
    Json.formatEnum(Therapy.Status)

  // Required to translate former Therapy StopReason values
  implicit def readsTherapyStatusReason(
    implicit reads: Reads[Coding[MTBTherapy.StatusReason.Value]]
  ): Reads[Coding[MTBTherapy.StatusReason.Value]] =
    reads.preprocess { 
      case js: JsObject =>
        (js \ "code").as[String] match {
          case "remission"                        => js + ("code" -> JsString("chronic-remission"))
          case "medical-reason"                   => js + ("code" -> JsString("medical-reasons"))
          case "unknown" | "continued-externally" => js + ("code" -> JsString("other"))
          case _ => js
        }
    }

  implicit val format: OFormat[MTBMedicationTherapy] =
    Json.format[MTBMedicationTherapy]

}
