package de.dnpm.dip.mtb.model


import cats.Applicative
import de.dnpm.dip.model.{
  Id,
  ExternalId,
  Patient,
  Reference,
  GeneAlterationReference,
  Quantity,
  UnitOfMeasure
}
import de.dnpm.dip.util.{
  Displays,
}
import de.dnpm.dip.coding.{
  Coding,
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
  Reads,
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


  implicit val displays: Displays[Variant] =
    Displays[Variant] {

      case snv: SNV =>
        s"SNV ${snv.gene.flatMap(_.display).getOrElse("")} ${snv.proteinChange.map(c => c.display.getOrElse(c.code.value)).getOrElse("")}"

      case cnv: CNV =>
        s"CNV ${cnv.reportedAffectedGenes.getOrElse(Set.empty).flatMap(_.display).mkString(",")} ${cnv.`type`.display.getOrElse("")}"

      case DNAFusion(_,_,partner5pr,partner3pr,_) =>
        s"DNA-Fusion ${partner5pr.gene.display.getOrElse("N/A")}-${partner3pr.gene.display.getOrElse("N/A")}"
      
      case RNAFusion(_,_,partner5pr,partner3pr,_,_,_) =>
        s"RNA-Fusion ${partner5pr.gene.display.getOrElse("N/A")}-${partner3pr.gene.display.getOrElse("N/A")}"
      
      case rnaSeq: RNASeq =>
        s"RNA-Seq ${rnaSeq.gene.flatMap(_.display).getOrElse("N/A")}"

    }


  implicit def displaysGeneAlteration(
    implicit res: Reference.Resolver[Variant]
  ): Displays[GeneAlterationReference[Variant]] =
    Displays[GeneAlterationReference[Variant]]{
      case GeneAlterationReference(gene,variant,_) =>
        s"${gene.flatMap(_.display).orElse(gene.map(_.code.value)).getOrElse("[Gene N/A]")} ${
          variant.resolve.map { 
            case snv: SNV => snv.proteinChange.map(c => c.display.getOrElse(c.code.value)).getOrElse("SNV")
            case cnv: CNV => cnv.`type`.display.getOrElse("CNV")
            case _: DNAFusion => "Fusion"
            case _: RNAFusion => "Fusion"
            case _: RNASeq    => "RNASeq"
          }
        }"
    }


  // Type class to check equivalence of variants,
  // i.e. if 2 variant object are conceptually the same variant 
  // irrespective of the patient reference on the object or interpretation values
  sealed trait Eq[T <: Variant] extends ((T,T) => Boolean)

  object Eq 
  {

    def apply[T <: Variant](implicit eq: Eq[T]) = eq

    private def instance[T <: Variant](f: (T,T) => Boolean): Eq[T] =
      new Eq[T]{
        override def apply(v1: T, v2: T) = f(v1,v2)
      }

    implicit val snvEq: Eq[SNV] =
      instance(
        (v1, v2) =>
          v1.gene == v2.gene &&
          v1.proteinChange == v2.proteinChange
      )
  
    implicit val cnvEq: Eq[CNV] =
      instance(
        (v1, v2) =>
          v1.reportedAffectedGenes == v2.reportedAffectedGenes &&
          v1.`type` == v2.`type`
      )

    implicit def fusionEq[F <: Fusion[_ <: { def gene: Coding[HGNC] }]]: Eq[F] =
      instance {
        (v1, v2) =>
          import scala.language.reflectiveCalls

          v1.fusionPartner5prime.gene == v2.fusionPartner5prime.gene &&
          v1.fusionPartner3prime.gene == v2.fusionPartner3prime.gene
      }

    implicit val rnaSeqEq: Eq[RNASeq] =
      instance(
        (v1, v2) =>
          v1.gene == v2.gene
      )

    implicit class syntax[T <: Variant](variant: T)(implicit eq: Eq[T])
    {
      def ===(other: T): Boolean =
        eq(variant,other)
    }

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
      chr20,
      chr21,
      chr22,
      chrX,
      chrY = Value

  override val display = {
    case chr => chr.toString
  }


  implicit val format: Format[Chromosome.Value] =
    Json.formatEnum(this)
}


sealed trait dbSNP
object dbSNP
{
  implicit val codingSystem: Coding.System[dbSNP] =
    Coding.System[dbSNP]("https://www.ncbi.nlm.nih.gov/snp/")
}

sealed trait COSMIC
object COSMIC
{
  implicit val codingSystem: Coding.System[COSMIC] =
    Coding.System[COSMIC]("https://cancer.sanger.ac.uk/cosmic")
}


sealed trait ClinVar
object ClinVar
extends CodedEnum("https://www.ncbi.nlm.nih.gov/clinvar/")
with DefaultCodeSystem
{
  val Zero  = Value("0")
  val One   = Value("1")
  val Two   = Value("2")
  val Three = Value("3")
  val Four  = Value("4")
  val Five  = Value("5")

  override val display =
    Map(
      Zero  -> "Not Applicable",
      One   -> "Benign",
      Two   -> "Likely benign",
      Three -> "Uncertain significance",
      Four  -> "Likely pathogenic",
      Five  -> "Pathogenic"
    )
}

sealed trait Transcript

final case class SNV
(
  id: Id[Variant],
  patient: Reference[Patient],
  externalIds: Set[ExternalId[SNV]],    // dbSNPId or COSMIC ID to be listed here
  chromosome: Coding[Chromosome.Value],
  gene: Option[Coding[HGNC]],
  transcriptId: Option[ExternalId[Transcript]],
  position: Variant.PositionRange,
  altAllele: SNV.Allele,
  refAllele: SNV.Allele,
  dnaChange: Option[Coding[HGVS.DNA]],
  proteinChange: Option[Coding[HGVS.Protein]],
  readDepth: SNV.ReadDepth,
  allelicFrequency: SNV.AllelicFrequency,
  interpretation: Option[Coding[ClinVar.Value]]
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
  reportedAffectedGenes: Option[Set[Coding[HGNC]]],
  reportedFocality: Option[String],
  `type`: Coding[CNV.Type.Value],
  copyNumberNeutralLoH: Option[Set[Coding[HGNC]]],
)
extends Variant


object CNV
{
  object Type
  extends CodedEnum("dnpm-dip/mtb/ngs-report/cnv/type")
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


sealed abstract class Fusion[Partner <: { def gene: Coding[HGNC] }] extends Variant
{
  val fusionPartner5prime: Partner
  val fusionPartner3prime: Partner
  val reportedNumReads: Int
}

final case class DNAFusion
(
  id: Id[Variant],
  patient: Reference[Patient],
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
    gene: Coding[HGNC],
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
  patient: Reference[Patient],
  fusionPartner5prime: RNAFusion.Partner,
  fusionPartner3prime: RNAFusion.Partner,
  effect: Option[String],
  externalIds: Set[ExternalId[RNAFusion]],  // COSMIC ID
  reportedNumReads: Int
)
extends Fusion[RNAFusion.Partner]

object RNAFusion
{

  object Strand extends Enumeration
  {
    val Plus  = Value("+")
    val Minus = Value("-")

    implicit val format: Format[Value] =
      Json.formatEnum(this)
  }

  final case class Partner
  (
    ids: Set[ExternalId[_]],  // Transcript ID and Exon Id listed here
    gene: Coding[HGNC],
    position: Long,
    strand: Strand.Value
  )

  implicit val formatPartner: OFormat[Partner] =
    Json.format[Partner]

  implicit val format: OFormat[RNAFusion] =
    Json.format[RNAFusion]
}


sealed trait Entrez
object Entrez
{
  implicit val codingSystem: Coding.System[Entrez] =
    Coding.System[Entrez]("Entrez")
}

final case class RNASeq
(
  id: Id[Variant],
  patient: Reference[Patient],
  ids: Set[ExternalId[_]],    // Entrez ID, Ensembl ID or Transcript ID to be listed here
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


