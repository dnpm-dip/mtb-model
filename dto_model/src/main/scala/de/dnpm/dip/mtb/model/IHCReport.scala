package de.dnpm.dip.mtb.model


import java.net.URI
import java.time.LocalDate
import cats.data.NonEmptyList
import de.dnpm.dip.model.{
  Id,
  DiagnosticReport,
  Patient,
  Reference,
  Observation,
}
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat,
}


final case class ProteinExpression
(
  id: Id[ProteinExpression],
  patient: Reference[Patient],
  protein: Coding[IHCReport.Protein],
  value: Coding[ProteinExpression.Result.Value],
  tpsScore: Option[Int],
  cpsScore: Option[Int],
  icScore: Option[Coding[ProteinExpression.ICScore.Value]],
  tcScore: Option[Coding[ProteinExpression.TCScore.Value]],
)
extends Observation[Coding[ProteinExpression.Result.Value]]


object ProteinExpression
{

  object Result
  extends CodedEnum("dnpm-dip/mtb/ihc/protein-expression/result")
  with DefaultCodeSystem
  {
    val Expressed    = Value("exp")
    val NotExpressed = Value("not-exp")
    val OnePlus      = Value("1+")
    val TwoPlus      = Value("2+")
    val ThreePlus    = Value("3+")
    val Unknown      = Value("unknown")

    override val display =
      Map(
        Expressed    -> "Exprimiert",
        NotExpressed -> "Nicht exprimiert",
        OnePlus      -> "1+",
        TwoPlus      -> "2+",
        ThreePlus    -> "3+",
        Unknown      -> "untersucht, kein Ergebnis"
      )

    implicit val format: Format[Result.Value] =
      Json.formatEnum(this)
  }

  object ICScore
  extends CodedEnum("dnpm-dip/mtb/ihc/protein-expression/ic-score")
  with DefaultCodeSystem
  {
    val Zero     = Value("0")
    val One      = Value("1")
    val Two      = Value("2")
    val Three    = Value("3")

    override val display =
      Map(
        Zero  -> "< 1%",
        One   -> ">= 1%",
        Two   -> ">= 5%",
        Three -> ">= 10%",
      )

    implicit val format: Format[ICScore.Value] =
      Json.formatEnum(this)
  }

  object TCScore
  extends CodedEnum("dnpm-dip/mtb/ihc/protein-expression/tc-score")
  with DefaultCodeSystem
  {
    val Zero     = Value("0")
    val One      = Value("1")
    val Two      = Value("2")
    val Three    = Value("3")
    val Four     = Value("4")
    val Five     = Value("5")
    val Six      = Value("6")

    override val display =
      Map(
        Zero  -> "< 1%",
        One   -> ">= 1%",
        Two   -> ">= 5%",
        Three -> ">= 10%",
        Four  -> ">= 25%",
        Five  -> ">= 50%",
        Six   -> ">= 75%",
      )

    implicit val format: Format[TCScore.Value] =
      Json.formatEnum(this)
  }


  implicit val format: OFormat[ProteinExpression] =
    Json.format[ProteinExpression]
}


final case class IHCReport
(
  id: Id[IHCReport],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  issuedOn: LocalDate,
  results: IHCReport.Results,
)
extends DiagnosticReport
{
  override val `type` = 
    Coding(
      code = "ihc",
      display = "Immun-Histo-Chemie",
      system = URI.create("dnpm-dip/mtb/ihc")
    )

  override val notes = None
}

object IHCReport
{

  sealed trait Protein
  object Protein extends CodeSystem.Publisher[Protein]
  {

    override implicit val codingSystem: Coding.System[Protein] = Coding.System("dnpm-dip/mtb/ihc/protein")

    val baseProteins = List(
      "ALK", "BRAF V600E", "EGFR", "HER2/neu",
      "AR", "ER", "PR", "Ki-67/MIB-1", "Nectin-4",
      "PanTRK", "PD-L1", "ROS1", "Trop-2",
      "ATRX", "CDK4", "CDK6", "CyclinD1", "CyclinD2",
      "CyclinD3", "MDM2", "MET", "p16", "p53",
      "p-AKT", "p-mTOR", "p-p38 MAPK", "p-p44/42 MAPK", "p-RB",
      "p-S6", "p-STAT3", "p-STAT5", "PTEN", "RB1"
    )

    val msiMmrProteins =
      Set("MLH1","MSH2","MSH6","PMS2")

    val msiMmrProteinFilter =
      CodeSystem.Filter[Protein](
        name = "MSI/MMR-Protein",
        description = Some("Filters MSI/MMR-Proteins"),              
        concept => msiMmrProteins.contains(concept.code.value)
      )

    override val filters = List(msiMmrProteinFilter)

    override val properties = Nil

    val codeSystem: CodeSystem[Protein] =
      CodeSystem[Protein](
        name = "IHC-Proteins",
        title = Some("IHC-Proteins"),
        version = None,
        concepts = (baseProteins ++ msiMmrProteins).map(code => (code,code)): _*  // Use code as display value
      )
  }



  final case class Results
  (
    proteinExpression: NonEmptyList[ProteinExpression],
    msiMmr: List[ProteinExpression]
  )


  // For Reads/Writes of NonEmptyList
  import de.dnpm.dip.util.json.{
    readsNel,
    writesNel
  }

  implicit val formatResults: OFormat[Results] =
    Json.format[Results]

  implicit val format: OFormat[IHCReport] =
    Json.format[IHCReport]
}
