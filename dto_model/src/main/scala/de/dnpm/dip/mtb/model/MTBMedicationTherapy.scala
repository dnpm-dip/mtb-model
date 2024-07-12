package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem,
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.model.{
  Id,
  Medications,
  Period,
  Reference,
  Patient,
  Therapy,
  MedicationTherapy
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}


final case class MTBMedicationTherapy
(
  id: Id[MTBMedicationTherapy],
  patient: Reference[Patient],
  indication: Option[Reference[MTBDiagnosis]],
  therapyLine: Option[Int],
  basedOn: Option[Reference[MTBMedicationRecommendation]],
  recordedOn: LocalDate,
  status: Coding[Therapy.Status.Value],
  statusReason: Option[Coding[Therapy.StatusReason.Value]],
  period: Option[Period[LocalDate]],
//  medication: Option[Set[Coding[Medications]]],
  medication: Option[Set[Coding[ATC]]],
  notes: Option[String]
)
extends MedicationTherapy[ATC]
//extends MedicationTherapy[Medications]
{
  val category = None
}


object MTBMedicationTherapy
{
  implicit val format: OFormat[MTBMedicationTherapy] =
    Json.format[MTBMedicationTherapy]
}
