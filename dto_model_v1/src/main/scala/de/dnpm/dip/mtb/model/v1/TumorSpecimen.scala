package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Reference,
  Patient
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodedEnum,
  DefaultCodeSystem,
}
import de.dnpm.dip.coding.icd.ICD10GM
import play.api.libs.json.{
  Json,
  OFormat
}
import de.dnpm.dip.mtb.model.TumorSpecimen.Type
import de.dnpm.dip.mtb.model.TumorSpecimen.Collection.{ 
  Localization,
  Method
}



final case class TumorSpecimen
(
  id: Id[TumorSpecimen],
  patient: Id[Patient],
  icd10: Coding[ICD10GM],
  `type`: Option[Type.Value],
  collection: Option[TumorSpecimen.Collection]
)


object TumorSpecimen
{

  final case class Collection
  (
    date: LocalDate,
    method: Method.Value,
    localization: Localization.Value
  )

  object Collection
  {
    implicit val format: OFormat[Collection] =
      Json.format[Collection]
  }


  implicit val format: OFormat[TumorSpecimen] =
    Json.format[TumorSpecimen]

}
