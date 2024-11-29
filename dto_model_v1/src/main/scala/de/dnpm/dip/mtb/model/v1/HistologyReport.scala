package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.icd.ICDO3
import de.dnpm.dip.model.{
  Id,
  Patient
}
import play.api.libs.json.{
  Json,
  OFormat
}


final case class TumorMorphology
(
  id: Id[TumorMorphology],
  patient: Id[Patient],
  specimen: Id[TumorSpecimen],
  value: Coding[ICDO3.Morphology],
  note: Option[String]
)

object TumorMorphology
{
  implicit val format: OFormat[TumorMorphology] =
    Json.format[TumorMorphology]
}


final case class HistologyReport
(
  id: Id[HistologyReport],
  patient: Id[Patient],
  specimen: Id[TumorSpecimen],
  issuedOn: LocalDate,
  tumorMorphology: Option[TumorMorphology],
  tumorCellContent: Option[TumorCellContent]
)

object HistologyReport
{

  implicit val format: OFormat[HistologyReport] =
    Json.format[HistologyReport]
}
