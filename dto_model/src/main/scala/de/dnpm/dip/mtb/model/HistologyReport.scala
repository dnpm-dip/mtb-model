package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.coding.icd.ICDO3
import de.dnpm.dip.model.{
  Id,
  Patient,
  Reference,
  Observation
}


final case class TumorMorphology
(
  id: Id[TumorMorphology],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  value: Coding[ICDO3.Morphology],
  notes: Option[String]
)
extends Observation[Coding[ICDO3.Morphology]]


final case class HistologyReport
(
  id: Id[HistologyReport],
  patient: Reference[Patient],
  specimen: Reference[TumorSpecimen],
  issuedOn: LocalDate,
  results: HistologyReport.Results
)

object HistologyReport
{

  final case class Results
  (
    tumorMorphology: Option[TumorMorphology],
    tumorCellContent: Option[TumorCellContent]
  )

}
