package de.dnpm.dip.mtb.model.v1


import de.dnpm.dip.model.Id
import de.dnpm.dip.coding.{
  Code,
  Coding
}
import de.dnpm.dip.coding.hgnc.{
  Ensembl,
  HGNC
}
import de.dnpm.dip.coding.hgvs.HGVS
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}
import de.dnpm.dip.mtb.model.{
  Chromosome,
  COSMIC,
  Entrez,
  Transcript,
}
import de.dnpm.dip.mtb.model.Variant.PositionRange
import de.dnpm.dip.mtb.model.SNV.{
  Allele,
  AllelicFrequency,
  ReadDepth
}
import de.dnpm.dip.mtb.model.CNV.{Type => CNVType}


sealed trait Exon

sealed abstract class Variant
{
  val id: Id[Variant]
}


final case class GeneCoding
(
  hgncId: Option[Code[HGNC]],
  ensemblId: Option[Code[Ensembl]]
)
object GeneCoding
{
  implicit val format: OFormat[GeneCoding] = 
    Json.format[GeneCoding]
}


final case class SNV
(
  id: Id[Variant],
  chromosome: Chromosome.Value,
  gene: Option[GeneCoding],
  startEnd: PositionRange,
  altAllele: Allele,
  refAllele: Allele,
  dnaChange: Option[Coding[HGVS.DNA]],
  aminoAcidChange: Option[Coding[HGVS.Protein]],
  readDepth: ReadDepth,
  allelicFrequency: AllelicFrequency,
  cosmicId: Option[Id[SNV]],
  dbSNPId: Option[Id[SNV]],
  interpretation: Option[Coding[Any]]
)
extends Variant

object SNV
{
  implicit val format: OFormat[SNV] =
    Json.format[SNV]
}


final case class CNV
(
  id: Id[Variant],
  chromosome: Chromosome.Value,
  startRange: PositionRange,
  endRange: PositionRange,
  totalCopyNumber: Option[Int],
  relativeCopyNumber: Option[Double],
  cnA: Option[Double],
  cnB: Option[Double],
  reportedAffectedGenes: Option[Set[GeneCoding]],
  reportedFocality: Option[String],
  `type`: CNVType.Value,
  copyNumberNeutralLoH: Option[Set[GeneCoding]],
)
extends Variant


object CNV
{
  implicit val formatType: Format[CNVType.Value] =
    Json.formatEnum(CNVType)

  implicit val format: OFormat[CNV] =
    Json.format[CNV]
}



sealed abstract class Fusion[Partner <: { def gene: GeneCoding }] extends Variant
{
  val fusionPartner5prime: Partner
  val fusionPartner3prime: Partner
  val reportedNumReads: Int
}

final case class DNAFusion
(
  id: Id[Variant],
  fusionPartner5prime: DNAFusion.Partner,
  fusionPartner3prime: DNAFusion.Partner,
  reportedNumReads: Int
)
extends Fusion[DNAFusion.Partner]

object DNAFusion
{

  final case class Partner
  (
    chromosome: Chromosome.Value,
    gene: GeneCoding,
    position: Long
  )

  implicit val formatPartner: OFormat[Partner] =
    Json.format[Partner]

  implicit val format: OFormat[DNAFusion] =
    Json.format[DNAFusion]
}


final case class RNAFusion
(
  id: Id[Variant],
  fusionPartner5prime: RNAFusion.Partner,
  fusionPartner3prime: RNAFusion.Partner,
  effect: Option[String],
  cosmicId: Option[Id[COSMIC]],
  reportedNumReads: Int
)
extends Fusion[RNAFusion.Partner]

object RNAFusion
{

  import de.dnpm.dip.mtb.model.RNAFusion.Strand

  final case class Partner
  (
    gene: GeneCoding,
    transcriptId: Id[Transcript],
    exon: Id[Exon],
    position: Long,
    strand: Strand.Value
  )

  implicit val formatPartner: OFormat[Partner] =
    Json.format[Partner]

  implicit val format: OFormat[RNAFusion] =
    Json.format[RNAFusion]
}


final case class RNASeq
(
  id: Id[Variant],
  entrezId: Id[Entrez],
  ensemblId: Id[Ensembl],
  gene: GeneCoding,
  transcriptId: Id[Transcript],
  fragmentsPerKilobaseMillion: Double,
  fromNGS: Boolean,
  tissueCorrectedExpression: Boolean,
  rawCounts: Int,
  librarySize: Int,
  cohortRanking: Option[Int]

)
extends Variant

object RNASeq
{
  implicit val format: OFormat[RNASeq] =
    Json.format[RNASeq]
}

