package de.dnpm.dip.mtb.model


import de.dnpm.dip.model.Patient


final case class MTBPatientRecord
(
  patient: Patient,
  episodes: List[MTBEpisode],
  diagnoses: List[MTBDiagnosis],
  performanceStatus: List[PerformanceStatus],
  specimens: List[TumorSpecimen],
  histologyReports: List[HistologyReport],
  ngsReports: List[NGSReport],
  carePlans: List[MTBCarePlan],
  medicationTherapies: List[MTBMedicationTherapy],
  responses: List[Response]
)
