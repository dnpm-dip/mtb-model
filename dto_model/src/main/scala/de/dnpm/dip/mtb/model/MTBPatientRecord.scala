package de.dnpm.dip.mtb.model


import de.dnpm.dip.model.Patient
import play.api.libs.json.{
  Json,
  OFormat
}


final case class MTBPatientRecord
(
  patient: Patient,
  episodes: List[MTBEpisode],
  diagnoses: List[MTBDiagnosis],
  guidelineMedicationTherapies: List[MTBMedicationTherapy],
  guidelineProcedures: List[OncoProcedure],
  performanceStatus: List[PerformanceStatus],
  specimens: List[TumorSpecimen],
  histologyReports: List[HistologyReport],
  ngsReports: List[NGSReport],
  carePlans: List[MTBCarePlan],
  medicationTherapies: List[MTBTherapyDocumentation],
  responses: List[Response]
)


object MTBPatientRecord
{

  implicit val format: OFormat[MTBPatientRecord] =
    Json.format[MTBPatientRecord]

}
