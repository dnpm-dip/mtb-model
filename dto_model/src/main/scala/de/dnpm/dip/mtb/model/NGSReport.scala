package de.dnpm.dip.mtb.model


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
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.hgvs.HGVS
import play.api.libs.json.{
  Json,
  Format,
  OFormat,
  Reads
}



final case class TMB
(
  id: Id[TMB],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  value: TMB.Result,
  interpretation: Option[Coding[TMB.Interpretation.Value]]
)
extends Observation[TMB.Result]

object TMB
{

  object Interpretation
  extends CodedEnum("mtb/ngs/tmb/interpretation")
  with DefaultCodeSystem
  {
    val Low          = Value("low")
    val Intermediate = Value("intermediate")
    val High         = Value("high")

  }


  val mutPerMBase =
    UnitOfMeasure("Mutations per megabase","mut/MBase")

  final case class Result(value: Double) extends Quantity 
  {
    override val unit = mutPerMBase
  }

  val referenceRange =
    ClosedInterval(Result(0.0) -> Result(1e6))

  implicit val readsResult: Reads[Result] =
    Json.reads[Result]

  implicit val format: OFormat[TMB] =
    Json.format[TMB]
}

/*
final case class MSI
(
  id: Id[MSI],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  value: MSI.Result
)
extends Observation[MSI.Result]

object MSI
{

  final case class Result(value: Double) extends AnyVal

  implicit val resultOrder: Ordering[Result] =
    Ordering[Double].on(_.value) 

  val referenceRange =
    LeftClosedInterval(Result(0.0))

  implicit val formatResult: Format[Result] =
    Json.valueFormat[Result]

  implicit val format: OFormat[MSI] =
    Json.format[MSI]
}
*/


final case class BRCAness
(
  id: Id[BRCAness],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  value: Double,
  confidenceRange: ClosedInterval[Double]
)
extends Observation[Double]

object BRCAness
{

  val referenceRange =
    ClosedInterval(0.0 -> 1.0)

  implicit val format: OFormat[BRCAness] =
    Json.format[BRCAness]
}



final case class HRDScore
(
  id: Id[HRDScore],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  value: Double,
  components: HRDScore.Components,
  interpretation: Option[Coding[HRDScore.Interpretation.Value]]
)
extends Observation[Double]

object HRDScore
{

  val referenceRange =
    ClosedInterval(0.0,100)


  object Interpretation
  extends CodedEnum("mtb/ngs/hrd-score/interpretation")
  with DefaultCodeSystem
  {
    val Low          = Value("low")
    val Intermediate = Value("intermediate")
    val High         = Value("high")

  }

  // Large-Scale Transitions: LST
  final case class LST(value: Double) extends AnyVal

  // Loss Of Heterozygocity: LoH
  final case class LoH(value: Double) extends AnyVal

  // Telomeric Allelic Imbalances: TAI
  final case class TAI(value: Double) extends AnyVal

  implicit val lstOrder: Ordering[LST] = Ordering[Double].on(_.value)
  implicit val lohOrder: Ordering[LoH] = Ordering[Double].on(_.value)
  implicit val taiOrder: Ordering[TAI] = Ordering[Double].on(_.value)

  val lstRefererenceRange = LeftClosedInterval(LST(0.0))
  val lohRefererenceRange = LeftClosedInterval(LoH(0.0))
  val taiRefererenceRange = LeftClosedInterval(TAI(0.0))

  final case class Components
  (
    lst: LST,
    loh: LoH,
    tai: TAI
  )

  implicit val formatLST: Format[LST] =
    Json.valueFormat[LST]

  implicit val formatLoH: Format[LoH] =
    Json.valueFormat[LoH]

  implicit val formatTAI: Format[TAI] =
    Json.valueFormat[TAI]

  implicit val formatComponents: OFormat[Components] =
    Json.format[Components]

  implicit val format: OFormat[HRDScore] =
    Json.format[HRDScore]

}



sealed abstract class Variant
{
  val id: Id[Variant]
  val patient: Reference[Patient]
}
object Variant
{

  final case class PositionRange
  (
    start: Long,
    end: Option[Long]
  )

  object PositionRange
  {
    implicit val format: OFormat[PositionRange] =
      Json.format[PositionRange]
  }

}

object Chromosome
extends CodedEnum("chromosome")
with DefaultCodeSystem
{
  val chr1,
      chr2,
      chr3,
      chr4,
      chr5,
      chr6,
      chr7,
      chr8,
      chr9,
      chr10,
      chr11,
      chr12,
      chr13,
      chr14,
      chr15,
      chr16,
      chr17,
      chr18,
      chr19,
      chr21,
      chr22 = Value

  val chrX = Value("X")
  val chrY = Value("Y")

  implicit val format: Format[Chromosome.Value] =
    Json.formatEnum(this)
}


sealed trait dbSNP
object dbSNP
{
  implicit val codingSystem =
    Coding.System[dbSNP]("https://www.ncbi.nlm.nih.gov/snp/")
}

sealed trait COSMIC
object COSMIC
{
  implicit val codingSystem =
    Coding.System[COSMIC]("https://cancer.sanger.ac.uk/cosmic")
}

sealed trait ClinVar
object ClinVar
{
  implicit val codingSystem =
    Coding.System[ClinVar]("https://www.ncbi.nlm.nih.gov/clinvar/")

  implicit val codeSystem: CodeSystem[ClinVar] =
    CodeSystem(
      name = "ClinVar-Interpretation",
      title = Some("ClinVar Interpretation"),
      version = None,
      "0" -> "Not Applicable",
      "1" -> "Benign",
      "2" -> "Likely benign",
      "3" -> "Uncertain significance",
      "4" -> "Likely pathogenic",
      "5" -> "Pathogenic"
    )
}

sealed trait Transcript

final case class SNV
(
  id: Id[Variant],
  patient: Reference[Patient],
//  codings: Set[Coding[_]],              // dbSNPId or COSMIC ID to be listed here
  externalIds: Set[ExternalId[SNV]],    // dbSNPId or COSMIC ID to be listed here
  chromosome: Coding[Chromosome.Value],
  gene: Option[Coding[HGNC]],
  transcriptId: ExternalId[Transcript],
  position: Variant.PositionRange,
  altAllele: SNV.Allele,
  refAllele: SNV.Allele,
  dnaChange: Option[Coding[HGVS]],
  aminoAcidChange: Option[Coding[HGVS]],
  readDepth: SNV.ReadDepth,
  allelicFrequency: SNV.AllelicFrequency,
  interpretation: Option[Coding[ClinVar]]
)
extends Variant

object SNV
{

  final case class Allele(value: String) extends AnyVal
  final case class AllelicFrequency(value: Double) extends AnyVal
  final case class ReadDepth(value: Int) extends AnyVal

  implicit val formatAllele: Format[Allele] =
    Json.valueFormat[Allele]

  implicit val formatAllelicFreq: Format[AllelicFrequency] =
    Json.valueFormat[AllelicFrequency]

  implicit val formatReadDepth: Format[ReadDepth] =
    Json.valueFormat[ReadDepth]

  implicit val format: OFormat[SNV] =
    Json.format[SNV]
}


final case class CNV
(
  id: Id[Variant],
  patient: Reference[Patient],
  chromosome: Coding[Chromosome.Value],
  startRange: Option[Variant.PositionRange],
  endRange: Option[Variant.PositionRange],
  totalCopyNumber: Option[Int],
  relativeCopyNumber: Option[Double],
  cnA: Option[Double],
  cnB: Option[Double],
  reportedAffectedGenes: List[Coding[HGNC]],
  reportedFocality: Option[String],
  `type`: Coding[CNV.Type.Value],
  copyNumberNeutralLoH: List[Coding[HGNC]],
)
extends Variant

object CNV
{
  object Type
  extends CodedEnum("mtb/ngs-report/cnv/type")
  with DefaultCodeSystem
  {
    val LowLevelGain  = Value("low-level-gain")
    val HighLevelGain = Value("high-level-gain")
    val Loss          = Value("loss")

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }

  implicit val format: OFormat[CNV] =
    Json.format[CNV]
}



sealed abstract class Fusion[Partner] extends Variant
{
  val fusionPartner5pr: Partner
  val fusionPartner3pr: Partner
  val reportedNumReads: Int
}

final case class DNAFusion
(
  id: Id[Variant],
  patient: Reference[Patient],
  fusionPartner5pr: DNAFusion.Partner,
  fusionPartner3pr: DNAFusion.Partner,
  reportedNumReads: Int
)
extends Fusion[DNAFusion.Partner]

object DNAFusion
{

  final case class Partner
  (
    chromosome: Chromosome.Value,
    position: Long,
    gene: Coding[HGNC]
  )

  implicit val formatPartner: OFormat[Partner] =
    Json.format[Partner]

  implicit val format: OFormat[DNAFusion] =
    Json.format[DNAFusion]
}


final case class RNAFusion
(
  id: Id[Variant],
  patient: Reference[Patient],
  fusionPartner5pr: RNAFusion.Partner,
  fusionPartner3pr: RNAFusion.Partner,
  reportedNumReads: Int
)
extends Fusion[RNAFusion.Partner]

object RNAFusion
{

  object Strand extends Enumeration
  {
    val Plus  = Value("+")
    val Minus = Value("-")

    implicit val format =
      Json.formatEnum(this)
  }

  final case class Partner
  (
    codings: Set[Reference[_]],  // Transcript ID and Exon Id listed here
//    codings: Set[Coding[_]],  // Transcript ID and Exon Id listed here
    position: Long,
    gene: Coding[HGNC],
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
  patient: Reference[Patient],
  codings: Set[Coding[_]],    // Entrez ID, Ensembl ID or Transcript ID to be listed here
  gene: Option[Coding[HGNC]],
  fragments: RNASeq.Fragments,
  fromNGS: Boolean,
  tissueCorrectedExpression: Boolean,
  rawCounts: Int,
  librarySize: Int,
  cohortRanking: Option[Int]
)
extends Variant

object RNASeq
{

  val fragmentsPerKbMillion =
    UnitOfMeasure("Fragments per Kb Million","fragments/Kb Million")

  final case class Fragments(value: Double) extends Quantity 
  {
    override val unit = fragmentsPerKbMillion
  }

  implicit val readsFragments: Reads[Fragments] =
    Json.reads[Fragments]

  implicit val format: OFormat[RNASeq] =
    Json.format[RNASeq]
}

final case class NGSReport
(
  id: Id[NGSReport],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  metadata: List[NGSReport.Metadata],
  results: NGSReport.Results
)

object NGSReport
{


  final case class Metadata
  (
    kitType: String,
    kitManufacturer: String,
    sequencer: String,
    referenceGenome: String,
    pipeline: URI
  )


  final case class Results
  (
    tumorCellContent: Option[TumorCellContent],
    brcaness: Option[BRCAness],
    hrdScore: Option[HRDScore],
//    msi: Option[MSI],
    simpleVariants: List[SNV],
    copyNumberVariants: List[CNV],
    dnaFusions: List[DNAFusion],
    rnaFusions: List[RNAFusion],
    rnaSeqs: List[RNASeq],
  )

  implicit val formatMetaData: OFormat[Metadata] =
    Json.format[Metadata]

  implicit val formatResults: OFormat[Results] =
    Json.format[Results]

  implicit val format: OFormat[NGSReport] =
    Json.format[NGSReport]
}
