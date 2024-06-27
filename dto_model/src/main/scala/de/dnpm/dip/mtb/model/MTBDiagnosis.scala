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
  tumorGrade: Option[Coding[TumorGrade.Value]],
  whoGrading: Option[Coding[WHOGrading]],
  //TODO: consider modelling "tumor grade" as
  // tumorGrade: Option[Coding[TumorGrade.Value :+: WHOGrading :+: CNil]]
  stageHistory: Option[Seq[MTBDiagnosis.TumorSpread]],
  guidelineTreatmentStatus: Option[Coding[GuidelineTreatmentStatus.Value]]
)
extends Diagnosis

object MTBDiagnosis
{

  object TumorSpread
  extends CodedEnum("dnpm-dip/mtb/diagnosis/tumor-spread") 
  with DefaultCodeSystem
  {
    val TumorFree    = Value("tumor-free")
    val Local        = Value("local")
    val Metastasized = Value("metastasized")
    val Unknown      = Value("unknown")

    implicit val format: Format[Value] =
      Json.formatEnum(this)

    override val display =
      Map(
        TumorFree    -> "Tumorfrei",
        Local        -> "Lokal",
        Metastasized -> "Metastasiert",
        Unknown      -> "Unbekannt"
      )
  }


  final case class TumorSpread
  (
    stage: Coding[TumorSpread.Value],
    date: LocalDate
  )


  implicit val formatStage: OFormat[TumorSpread] =
    Json.format[TumorSpread]

  implicit val format: OFormat[MTBDiagnosis] =
    Json.format[MTBDiagnosis]

}

