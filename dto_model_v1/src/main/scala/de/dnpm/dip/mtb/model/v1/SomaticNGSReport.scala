package de.dnpm.dip.mtb.model.v1


import java.net.URI
import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.util.json
import de.dnpm.dip.model.{
  Id,
  ExternalId,
  Patient,
  Reference,
  ClosedInterval,
  LeftClosedInterval,
  NGSReport,
  Observation,
  Quantity,
  UnitOfMeasure
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat,
  Reads,
  JsonValidationError,
  Writes
}
import de.dnpm.dip.mtb.model.SomaticNGSReport.Metadata



final case class SomaticNGSReport
(
  id: Id[SomaticNGSReport],
  patient: Id[Patient],
  specimen: Id[TumorSpecimen],
  issueDate: LocalDate,
  sequencingType: String,
//  sequencingType: NGSReport.SequencingType.Value,
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
  
  implicit val formatSequencingType: Format[NGSReport.SequencingType.Value] =
    json.enumFormat(NGSReport.SequencingType)

  implicit val format: OFormat[SomaticNGSReport] =
    Json.format[SomaticNGSReport]

}
