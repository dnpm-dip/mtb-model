package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.model.{
  Id,
  Reference,
  Patient
}
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
}
import play.api.libs.json.{
  Json,
  OFormat
}



final case class TumorSpecimen
(
  id: Id[TumorSpecimen],
  patient: Reference[Patient],
  diagnosis: Reference[MTBDiagnosis],
  `type`: Coding[TumorSpecimen.Type.Value],
  collection: Option[TumorSpecimen.Collection]
)


object TumorSpecimen
{

  object Type
  extends CodedEnum("dnpm-dip/mtb/tumor-specimen/type")
  with DefaultCodeSystem
  {
    val FreshTissue  = Value("fresh-tissue")
    val CryoFrozen   = Value("cryo-frozen")
    val LiquidBiopsy = Value("liquid-biopsy")
    val FFPE         = Value("FFPE")
    val Unknown      = Value("unknown")

    override val display =
      Map(
        FreshTissue  -> "Frischgewebe",
        CryoFrozen   -> "Cryo-frozen",
        LiquidBiopsy -> "Liquid Biopsy",
        FFPE         -> "FFPE",
        Unknown      -> "Unbekannt"
      )
  }

  final case class Collection
  (
    date: Option[LocalDate],
    method: Coding[Collection.Method.Value],
    localization: Coding[Collection.Localization.Value]
  )

  object Collection
  {
    
    object Method
    extends CodedEnum("dnpm-dip/mtb/tumor-specimen/collection/method")
    with DefaultCodeSystem
    {
      val Biopsy       = Value("biopsy")
      val Resection    = Value("resection")
      val LiquidBiopsy = Value("liquid-biopsy")
      val Cytology     = Value("cytology")
      val Unknown      = Value("unknown") 

      override val display =
        Map(
          Biopsy       -> "Biopsie",
          Resection    -> "Resektat",
          LiquidBiopsy -> "Liquid Biopsy",
          Cytology     -> "Zytologie",
          Unknown      -> "Unbekannt"
        )
    }

    object Localization
    extends CodedEnum("dnpm-dip/mtb/tumor-specimen/collection/localization")
    with DefaultCodeSystem
    {
      val PrimaryTumor       = Value("primary-tumor")
      val Metastatis         = Value("metastasis")
      val LocalRecurrence    = Value("local-recurrence")
      val RegionalLymphNodes = Value("regional-lymph-nodes")
      val CellfreeDNA        = Value("cellfree-dna")
      val Unknown            = Value("unknown")

      override val display =
        Map(
          PrimaryTumor       -> "Primärtumor",
          Metastatis         -> "Metastase",
          LocalRecurrence    -> "Lokalrezidiv", 
          RegionalLymphNodes -> "Regionäre Lymphknoten",
          CellfreeDNA        -> "Zellfreie DNA",
          Unknown            -> "Unbekannt"
        )
    }

    implicit val format: OFormat[Collection] =
      Json.format[Collection]

  }


  implicit val format: OFormat[TumorSpecimen] =
    Json.format[TumorSpecimen]

}
