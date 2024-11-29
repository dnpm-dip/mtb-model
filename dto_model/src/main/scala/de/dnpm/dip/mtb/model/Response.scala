package de.dnpm.dip.mtb.model


import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.model.{
  Id,
  Patient,
  Observation,
  Reference
}
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystemProvider,
  CodeSystemProviderSPI,
  DefaultCodeSystem
}
import play.api.libs.json.{
  Json,
  OFormat
}


object RECIST
extends CodedEnum("RECIST")
with DefaultCodeSystem
{

  val CR  = Value("CR")
  val PR  = Value("PR")
  val MR  = Value("MR")
  val SD  = Value("SD")
  val PD  = Value("PD")
  val NA  = Value("NA")
  val NYA = Value("NYA")

  override val display = {
    case CR  => "Complete Response"
    case PR  => "Partial Response"
    case MR  => "Mixed Response"
    case SD  => "Stable Disease"
    case PD  => "Progressive Disease"
    case NA  => "Not Assessable"
    case NYA => "Not Yet Assessable"
  }

  final class ProviderSPI extends CodeSystemProviderSPI
  {
    override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
      new Provider.Facade[F]
  }

}



final case class Response
(
  id: Id[Response],
  patient: Reference[Patient],
  therapy: Reference[MTBMedicationTherapy],
  effectiveDate: LocalDate,
  value: Coding[RECIST.Value]
)
extends Observation[Coding[RECIST.Value]]

object Response
{
  implicit val format: OFormat[Response] =
    Json.format[Response]
}
