package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Reference,
  Quantity,
  Observation,
  Patient
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodedEnum,
  DefaultCodeSystem
}


object ECOG
extends CodedEnum("ECOG-Performance-Status")
with DefaultCodeSystem
{
  val Zero  = Value("0")
  val One   = Value("1")
  val Two   = Value("2")
  val Three = Value("3")
  val Four  = Value("4")
  

/*
  implicit val codeSystem: CodeSystem[Value] =
    CodeSystem(
      Coding.System[Value].uri,
      "ECOG-Performance-Status",
      Some("ECOG-Performance-Status"),
      Zero  -> "ECOG 0",
      One   -> "ECOG 1",
      Two   -> "ECOG 2",
      Three -> "ECOG 3",
      Four  -> "ECOG 4"
    )
*/
}


final case class PerformanceStatus
(
  id: Id[PerformanceStatus],
  patient: Reference[Patient],
  effectiveDate: LocalDate,
  value: Coding[ECOG.Value]
)
extends Observation[Coding[ECOG.Value]]
