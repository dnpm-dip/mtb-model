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
  DefaultCodeSystem
}
import de.dnpm.dip.coding.hgnc.HGNC
import play.api.libs.json.{
  Json,
  Format,
  OFormat,
}

final case class ProteinExpression
(
  id: Id[ProteinExpression],
  patient: Reference[Patient],
  protein: Coding[HGNC],
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
      "ihc",
      "Immun-Histo-Chemie",
      URI.create("dnpm-dip/mtb/ihc")
    )

  override val notes =
    None
}

object IHCReport
{

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
