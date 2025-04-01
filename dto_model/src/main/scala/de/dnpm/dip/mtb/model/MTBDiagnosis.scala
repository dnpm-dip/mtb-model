package de.dnpm.dip.mtb.model


import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI
}
import de.dnpm.dip.model.{
  Id,
  History,
  Reference,
  Patient,
  Diagnosis,
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
  `type`: History[MTBDiagnosis.Type],
  code: Coding[ICD10GM],
  germlineCodes: Option[Set[Coding[ICD10GM]]],
  topography: Option[Coding[ICDO3.T]],
  grading: Option[History[TumorGrading]],
  staging: History[TumorStaging], // TODO: Make Optional again?
  guidelineTreatmentStatus: Option[Coding[MTBDiagnosis.GuidelineTreatmentStatus.Value]],
  histology: Option[List[Reference[HistologyReport]]],
  notes: Option[List[String]]
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

  object GuidelineTreatmentStatus
  extends CodedEnum("dnpm-dip/mtb/diagnosis/guideline-treatment-status")
  with DefaultCodeSystem
  {
 
    val Exhaustive            = Value("exhausted")
    val NonExhaustive         = Value("non-exhausted")
    val Impossible            = Value("impossible")
    val NoGuidelinesAvailable = Value("no-guidelines-available")
    val Unknown               = Value("unknown")
 
    override val display =
      Map(
        Exhaustive            -> "Leitlinien ausgeschöpft",
        NonExhaustive         -> "Leitlinien nicht ausgeschöpft",
        Impossible            -> "Leitlinientherapie nicht möglich",
        NoGuidelinesAvailable -> "Keine Leitlinien vorhanden",
        Unknown               -> "Unbekannt"
      )
 
    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }
  }

  implicit val format: OFormat[MTBDiagnosis] =
    Json.format[MTBDiagnosis]

}

