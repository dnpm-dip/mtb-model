package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem,
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.icd.ICD10GM
import de.dnpm.dip.model.{
  Id,
  Period,
  Patient,
  Therapy
}
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
  notDoneReason: Option[Coding[Therapy.StatusReason.Value]],
  reasonStopped: Option[Coding[Therapy.StatusReason.Value]],
  note: Option[String],
)


object MTBMedicationTherapy
{

  implicit val formatTherapyStatus: Format[Therapy.Status.Value] =
    Json.formatEnum(Therapy.Status)


  // Required to translate former Therapy StopReason value "remission" into "chronic-remission"
  implicit def readsTherapyStatusReason(
    implicit reads: Reads[Coding[Therapy.StatusReason.Value]]
  ): Reads[Coding[Therapy.StatusReason.Value]] =
    reads.preprocess { 
      case js: JsObject if (js \ "code").as[String] == "remission" =>
        js + ("code" -> JsString("chronic-remission"))
    }

  implicit val format: OFormat[MTBMedicationTherapy] =
    Json.format[MTBMedicationTherapy]

}
