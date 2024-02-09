package de.dnpm.dip.mtb.model.v1


import java.net.URI
import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.model.{
  Id,
  ExternalId,
  Patient,
  Reference,
  ClosedInterval,
  LeftClosedInterval,
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
  Reads
}
import de.dnpm.dip.mtb.model.NGSReport.Metadata



final case class NGSReport
(
  id: Id[NGSReport],
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

object NGSReport
{
  implicit val format: OFormat[NGSReport] =
    Json.format[NGSReport]
}
