package de.dnpm.dip.mtb.model


import java.net.URI
import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.model.{
  Id,
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
  extends CodedEnum("dnpm-dip/mtb/ngs/tmb/interpretation")
  with DefaultCodeSystem
  {
    val Low          = Value("low")
    val Intermediate = Value("intermediate")
    val High         = Value("high")

    override val display =
      Map(
        Low          -> "Niedrig",
        Intermediate -> "Mittel",
        High         -> "Hoch"
      )

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }
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
  extends CodedEnum("dnpm-dip/mtb/ngs/hrd-score/interpretation")
  with DefaultCodeSystem
  {
    val Low          = Value("low")
    val Intermediate = Value("intermediate")
    val High         = Value("high")

    override val display =
      Map(
        Low          -> "Niedrig",
        Intermediate -> "Mittel",
        High         -> "Hoch"
      )

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }
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


final case class SomaticNGSReport
(
  id: Id[SomaticNGSReport],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  issuedOn: LocalDate,
  `type`: Coding[NGSReport.Type.Value],
  metadata: List[SomaticNGSReport.Metadata],
  results: SomaticNGSReport.Results
)
extends NGSReport
{
  def variants: List[Variant] =
    results.simpleVariants ++
    results.copyNumberVariants ++
    results.dnaFusions ++
    results.rnaFusions

  override val notes =
    None
}

object SomaticNGSReport
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
    tmb: Option[TMB],
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

  implicit val format: OFormat[SomaticNGSReport] =
    Json.format[SomaticNGSReport]
}
