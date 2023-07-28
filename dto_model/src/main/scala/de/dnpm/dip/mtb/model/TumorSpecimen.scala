package de.dnpm.dip.mtb.model


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


final case class TumorSpecimen
(
  id: Id[TumorSpecimen],
  patient: Reference[Patient],
  `type`: TumorSpecimen.Type.Value,
  collection: Option[TumorSpecimen.Collection]
)


object TumorSpecimen
{

  object Type
  extends CodedEnum("mtb/tumor-specimen/type")
  with DefaultCodeSystem
  {
    val FreshTissue  = Value("fresh-tissue")
    val CryoFrozen   = Value("cryo-frozen")
    val LiquidBiopsy = Value("liquid-biopsy")
    val FFPE         = Value("FFPE")
    val Unknown      = Value("unknown")

  }

  final case class Collection
  (
    date: LocalDate,
    method: Collection.Method.Value,
    localization: Collection.Localization.Value
  )

  object Collection
  {
    
    object Method
    extends CodedEnum("mtb/tumor-specimen/collection/method")
    with DefaultCodeSystem
    {
      val Biopsy       = Value("biopsy")
      val Resection    = Value("resection")
      val LiquidBiopsy = Value("liquid-biopsy")
      val Cytology     = Value("cytology")
      val Unknown      = Value("unknown")
 
    }

    object Localization
    extends CodedEnum("mtb/tumor-specimen/collection/localization")
    with DefaultCodeSystem
    {
      val PrimaryTumor = Value("primary-tumor")
      val Metastatis   = Value("metastasis")
      val Unknown      = Value("unknown")
 
    }

  }

}
