package de.dnpm.dip.mtb.model


import java.time.LocalDate
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
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
  OFormat
}



final case class MTBDiagnosis
(
  id: Id[MTBDiagnosis],
  patient: Reference[Patient],
  recordedOn: Option[LocalDate],
  `type`: NonEmptyList[MTBDiagnosis.Type],
  code: Coding[ICD10GM],
  germlineCodes: Option[Coding[ICD10GM]],
  topography: Option[Coding[ICDO3.T]],
  grading: Option[List[TumorGrading]],
  staging: NonEmptyList[TumorStaging],
  guidelineTreatmentStatus: Option[Coding[GuidelineTreatmentStatus.Value]],
  histology: Option[List[Reference[HistologyReport]]]
)
extends Diagnosis



object MTBDiagnosis
{

  final case class Type
  (
    value: Coding[Type.Value],
    date: LocalDate
  )

  object Type
  extends CodedEnum("dnpm-dip/mtb/diagnosis/type")
  with DefaultCodeSystem
  {
    val Main         = Value("main")
    val Secondary    = Value("secondary")
    val Metachronous = Value("metachronous")

    override val display =
      Map(
        Main         -> "Hauptdiagnose",
        Secondary    -> "Nebendiagnose",
        Metachronous -> "Metachron"
      )

    implicit val format: OFormat[Type] =
      Json.format[Type]
  }


  // For Reads/Writes of NonEmptyList
  import de.dnpm.dip.util.json.{
    readsNel,
    writesNel
  }

  implicit val format: OFormat[MTBDiagnosis] =
    Json.format[MTBDiagnosis]

}

