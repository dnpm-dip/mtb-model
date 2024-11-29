package de.dnpm.dip.mtb.model.v1


import de.dnpm.dip.model.Id
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}
import de.dnpm.dip.mtb.model.TumorCellContent.Method


final case class TumorCellContent
(
  id: Id[TumorCellContent],
  specimen: Id[TumorSpecimen],
  method: Method.Value,
  value: Double,
)


object TumorCellContent
{

  implicit val formatMethod: Format[Method.Value] =
    Json.formatEnum(Method)

  implicit val format: OFormat[TumorCellContent] =
    Json.format[TumorCellContent]
}
