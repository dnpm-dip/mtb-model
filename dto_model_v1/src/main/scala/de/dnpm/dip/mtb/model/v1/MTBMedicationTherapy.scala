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
  Format,
  OFormat
}


final case class MTBMedicationTherapy
(
  id: Id[MTBMedicationTherapy],
  patient: Id[Patient],
  diagnosis: Option[Id[MTBDiagnosis]],
  therapyLine: Option[Int],
  basedOn: Option[Id[MTBMedicationRecommendation]],
//  recordedOn: LocalDate,
  recordedOn: Option[LocalDate],
  status: Option[Therapy.Status.Value],
  period: Option[Period[LocalDate]],
  medication: Option[Set[Coding[ATC]]],
  notDoneReason: Option[Coding[Therapy.StatusReason]],
  reasonStopped: Option[Coding[Therapy.StatusReason]],
  note: Option[String],
)


object MTBMedicationTherapy
{

  implicit val formatTherapyStatus: Format[Therapy.Status.Value] =
    Json.formatEnum(Therapy.Status)

  implicit val format: OFormat[MTBMedicationTherapy] =
    Json.format[MTBMedicationTherapy]

}
