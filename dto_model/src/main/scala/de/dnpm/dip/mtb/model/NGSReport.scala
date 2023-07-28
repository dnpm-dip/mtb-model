package de.dnpm.dip.mtb.model


import java.net.URI
import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
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
}
import de.dnpm.dip.coding.hgnc.HGNC


final case class TMB
(
  id: Id[TMB],
  patient: Reference[Patient],
  effectiveDate: LocalDate,
  specimen: Reference[TumorSpecimen],
  value: TMB.Result,
)
extends Observation[TMB.Result]

object TMB
{

  val mutPerMBase =
    UnitOfMeasure("Mutations per megabase","mut/MBase")

  final case class Result(value: Double) extends Quantity 
  {
    override val unit = mutPerMBase
  }

  val referenceRange =
    ClosedInterval(Result(0.0) -> Result(1e6))

}


final case class MSI
(
  id: Id[MSI],
  patient: Reference[Patient],
  effectiveDate: LocalDate,
  specimen: Reference[TumorSpecimen],
  value: MSI.Result
)
extends Observation[MSI.Result]

object MSI
{

  final case class Result(value: Double) extends AnyVal

  implicit val resultOrder: Ordering[Result] = Ordering[Double].on(_.value) 

  val referenceRange =
    LeftClosedInterval(Result(0.0))

}


final case class HRDScore
(
  id: Id[HRDScore],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  effectiveDate: LocalDate,
  value: Double,
  components: HRDScore.Components,
  interpretation: Option[HRDScore.Interpretation.Value]
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

}



sealed abstract class Variant
{
  val id: Id[Variant]
}
object Variant
{

  final case class PositionRange
  (
    start: Long,
    end: Option[Long]
  )
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
      chr22,
      chrX,
      chrY = Value
}


object HGVS
{
  sealed trait c
  sealed trait p

  implicit val codingSystemDNA =
    Coding.System[HGVS.c]("https://varnomen.hgvs.org/recommendations/DNA/")

  implicit val codingSystemProtein =
    Coding.System[HGVS.p]("https://varnomen.hgvs.org/recommendations/protein/")
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
}


final case class SNV
(
  id: Id[SNV],
  codings: Set[Coding[_]],    // dbSNPId or COSMIC ID to be listed here
  chromosome: Chromosome.Value,
  gene: Option[Coding[HGNC]],
  position: Variant.PositionRange,
  altAllele: SNV.Allele,
  refAllele: SNV.Allele,
  dnaChange: Option[Coding[HGVS.c]],
  aminoAcidChange: Option[Coding[HGVS.p]],
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

}


final case class CNV
(
  id: Id[CNV],
  chromosome: Chromosome.Value,
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

  }

}



sealed abstract class Fusion[Partner] extends Variant
{
  val fusionPartner5pr: Partner
  val fusionPartner3pr: Partner
  val reportedNumReads: Int
}

final case class DNAFusion
(
  id: Id[DNAFusion],
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

}


final case class RNAFusion
(
  id: Id[RNAFusion],
  fusionPartner5pr: RNAFusion.Partner,
  fusionPartner3pr: RNAFusion.Partner,
  reportedNumReads: Int
)
extends Fusion[RNAFusion.Partner]

object RNAFusion
{

  object Strand
  extends CodedEnum("mtb/ngs-report/rna-fusion/strand")
  with DefaultCodeSystem
  {
    val Plus  = Value("+")
    val Minus = Value("-")
  }

  final case class Partner
  (
    codings: Set[Coding[_]],  // Transcript ID and Exon Id listed here
    position: Long,
    gene: Coding[HGNC],
    strand: Strand.Value
  )

}


final case class RNASeq
(
  id: Id[RNASeq],
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
    hrdScore: Option[HRDScore],
    msi: Option[MSI],
    simpleVariants: List[SNV],
    copyNumberVariants: List[CNV],
    dnaFusions: List[DNAFusion],
    rnaFusions: List[RNAFusion],
    rnaSeqs: List[RNASeq],
  )

}
