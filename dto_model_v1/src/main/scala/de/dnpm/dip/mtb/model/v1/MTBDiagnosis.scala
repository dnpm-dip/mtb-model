package de.dnpm.dip.mtb.model.v1


import java.net.URI
import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
  Reference,
  Patient,
  Diagnosis,
  GuidelineTreatmentStatus
}
import de.dnpm.dip.coding.icd.{
  ICD10GM,
  ICDO3
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}
import de.dnpm.dip.mtb.model.WHOGrading


final case class MTBDiagnosis
(
  id: Id[MTBDiagnosis],
  patient: Id[Patient],
  recordedOn: Option[LocalDate],
  icd10: Coding[ICD10GM],
  icdO3T: Option[Coding[ICDO3.Topography]],
  whoGrade: Option[Coding[WHOGrading]],
  histologyResults: Option[List[Id[HistologyReport]]],
  statusHistory: Option[List[MTBDiagnosis.StatusOnDate]],
  guidelineTreatmentStatus: Option[GuidelineTreatmentStatus.Value]
)


object MTBDiagnosis
{

  import de.dnpm.dip.mtb.model.MTBDiagnosis.TumorSpread

  final case class StatusOnDate
  (
    status: TumorSpread.Value,
    date: LocalDate
  )


  implicit val formatGuidelineTreatmentStatus: Format[GuidelineTreatmentStatus.Value] =
    Json.formatEnum(GuidelineTreatmentStatus)

  implicit val formatStatus: OFormat[StatusOnDate] =
    Json.format[StatusOnDate]

  implicit val format: OFormat[MTBDiagnosis] =
    Json.format[MTBDiagnosis]

}
