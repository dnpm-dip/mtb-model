package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Patient
}
import play.api.libs.json.{
  Json,
  OFormat
}
import de.dnpm.dip.mtb.model.SomaticNGSReport.Metadata


final case class SomaticNGSReport
(
  id: Id[SomaticNGSReport],
  patient: Id[Patient],
  specimen: Id[TumorSpecimen],
  issueDate: LocalDate,
  sequencingType: String,
  metadata: List[Metadata],
  tumorCellContent: Option[TumorCellContent],
  brcaness: Option[Double],
  msi: Option[Double],
  tmb: Option[Double],
  simpleVariants: Option[List[SNV]],
  copyNumberVariants: Option[List[CNV]],
  dnaFusions: Option[List[DNAFusion]],
  rnaFusions: Option[List[RNAFusion]],
  rnaSeqs: Option[List[RNASeq]]
)

object SomaticNGSReport
{
  
  implicit val format: OFormat[SomaticNGSReport] =
    Json.format[SomaticNGSReport]

}
