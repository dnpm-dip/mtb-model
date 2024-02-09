package de.dnpm.dip.mtb.model


import java.time.Year
import java.net.URI
import cats.Applicative
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
  Version,
  CodeSystem,
  CodeSystemProvider,
  BasicCodeSystemProvider,
  CodeSystemProviderSPI
}


sealed trait WHOGrading

object WHOGrading
{

  val I   = "I"
  val II  = "II"
  val III = "III"
  val IV  = "IV"

  val One   = "1"
  val Two   = "2"
  val Three = "3"
  val Four  = "4"


  implicit val codingSystem: Coding.System[WHOGrading] =
    Coding.System[WHOGrading]("mtb/who-grading-cns-tumors")


  val codeSystem4th: CodeSystem[WHOGrading] =
    CodeSystem(
      uri     = Coding.System[WHOGrading].uri,
      name    = "WHO-Grading-CNS-Tumors",
      title   = Some("WHO-Grading of CNS Tumors, 4th edition"),
      version = Some("2016"),
      I   -> "Pilocytic astrocytoma",
      II  -> "Diffuse astrocytoma",
      III -> "Anaplastic astrocytoma",
      IV  -> "Glioblastoma"
    )

  val codeSystem5th: CodeSystem[WHOGrading] =
    CodeSystem(
      uri     = Coding.System[WHOGrading].uri,
      name    = "WHO-Grading-CNS-Tumors",
      title   = Some("WHO-Grading of CNS Tumors, 5th edition"),
      version = Some("2021"),
      One   -> "Pilocytic astrocytoma",
      Two   -> "Diffuse astrocytoma",
      Three -> "Anaplastic astrocytoma",
      Four  -> "Glioblastoma"
    )


  object Provider extends BasicCodeSystemProvider[WHOGrading](
    Version.OrderedByYear,
    codeSystem4th,
    codeSystem5th
  )  


  final class ProviderSPI extends CodeSystemProviderSPI
  {
    override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
      new Provider.Facade[F]
  }


}

