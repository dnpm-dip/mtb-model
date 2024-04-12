package de.dnpm.dip.mtb.model


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



final case class MTBDiagnosis
(
  id: Id[MTBDiagnosis],
  patient: Reference[Patient],
  recordedOn: Option[LocalDate],
  code: Coding[ICD10GM],
  topography: Option[Coding[ICDO3.Topography]],
  whoGrading: Option[Coding[WHOGrading]],
  stageHistory: Option[Seq[MTBDiagnosis.StageOnDate]],
  guidelineTreatmentStatus: Option[Coding[GuidelineTreatmentStatus.Value]]
)
extends Diagnosis

object MTBDiagnosis
{

  object TumorStage
  extends CodedEnum("dnpm-dip/mtb/diagnosis/stage")
  with DefaultCodeSystem
  {
    type Stage = Value

    val TumorFree    = Value("tumor-free")
    val Local        = Value("local")
    val Metastasized = Value("metastasized")
    val Unknown      = Value("unknown")

    implicit val format: Format[Value] =
      Json.formatEnum(this)

    override val display = {
      case TumorFree    => "Tumorfrei"
      case Local        => "Lokal"
      case Metastasized => "Metastasiert"
      case Unknown      => "Unbekannt"
    }
  }


  final case class StageOnDate
  (
    stage: Coding[TumorStage.Value],
    date: LocalDate
  )


  implicit val formatStage: OFormat[StageOnDate] =
    Json.format[StageOnDate]

  implicit val format: OFormat[MTBDiagnosis] =
    Json.format[MTBDiagnosis]

}

