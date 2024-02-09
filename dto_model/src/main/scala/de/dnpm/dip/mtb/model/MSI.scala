
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
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat,
  Reads
}



final case class MSI
(
  id: Id[MSI],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  method: Coding[MSI.Method.Value],
  value: MSI.Result,
  interpretation: Coding[MSI.Interpretation.Value]
)
extends Observation[MSI.Result]


object MSI
{

  object Method
  extends CodedEnum("dnpm-dip/mtb/msi/method")
  with DefaultCodeSystem
  {
    val IHC           = Value("IHC")
    val PCR           = Value("PCR")
    val Bioinformatic = Value("bioinformatic")

    override val display = {
      case Bioinformatic => "Sequenzierung"
      case x             => x.toString
    }
  }

  object Interpretation
  extends CodedEnum("dnpm-dip/mtb/msi/interpretation")
  with DefaultCodeSystem
  {

    val MSS           = Value("stable")  
    val MSILow        = Value("msi-low")   
    val MSIHigh       = Value("msi-high")   
    val MMRProficient = Value("mmr-proficient") 
    val MMRDeficient  = Value("mmr-deficient") 
    val Unknown       = Value("unknown") 

    override val display =
      Map(
        MSS           -> "MSS",
        MSILow        -> "MSI low",
        MSIHigh       -> "MSI high",
        MMRProficient -> "MMR Proficient",
        MMRDeficient  -> "MMR Deficient",
        Unknown       -> "Unbekannt" 
      )

  }


  final case class Result(value: Double) extends AnyVal

  object Result
  {

    implicit val resultOrder: Ordering[Result] =
      Ordering[Double].on(_.value) 
  
    val referenceRange =
      LeftClosedInterval(Result(0.0))

  }


  implicit val formatResult: Format[Result] =
    Json.valueFormat[Result]

  implicit val format: OFormat[MSI] =
    Json.format[MSI]

}
