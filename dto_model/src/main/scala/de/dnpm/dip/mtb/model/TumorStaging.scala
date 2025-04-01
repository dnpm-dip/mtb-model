package de.dnpm.dip.mtb.model


import java.time.LocalDate
import shapeless.{
  :+:,
  CNil
}
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}
import play.api.libs.json.{ 
  Json,
  OFormat
}


final case class TumorStaging
(
  date: LocalDate,
  method: Coding[TumorStaging.Method.Value],
  tnmClassification: TumorStaging.TNM, 
  otherClassifications: Option[List[Coding[TumorStaging.OtherSystems]]]
)


object TumorStaging
{

  object Method
  extends CodedEnum("dnpm-dip/mtb/tumor-staging/method")
  with DefaultCodeSystem
  {
    val Clinical   = Value("clinical")
    val Pathologic = Value("pathologic")

    override val display =
      Map(
        Clinical   -> "Klinisch",
        Pathologic -> "Pathologisch"
      )
  }


  final case class TNM
  (
    tumor: Coding[TNM.Systems],
    nodes: Coding[TNM.Systems],
    metastasis: Coding[TNM.Systems]
  )

  object TNM
  {

    sealed trait UICC
    sealed trait AJCC

    implicit val uiccSystem: Coding.System[UICC] =
      Coding.System("UICC")

    implicit val ajccSystem: Coding.System[AJCC] =
      Coding.System("AJCC")

    type Systems = UICC :+: AJCC :+: CNil

    implicit val format: OFormat[TNM] =
      Json.format[TNM]

  }


  sealed trait FIGO
  object FIGO
  {
    implicit val system: Coding.System[FIGO] =
      Coding.System("FIGO")
  }


  object KDSSpread
  extends CodedEnum("dnpm-dip/mtb/diagnosis/kds-tumor-spread")
  with DefaultCodeSystem
  {
    val TumorFree    = Value("tumor-free")
    val Local        = Value("local")
    val Metastasized = Value("metastasized")
    val Unknown      = Value("unknown")

    override val display =
      Map(
        TumorFree    -> "Tumorfrei",
        Local        -> "Lokal",
        Metastasized -> "Metastasiert",
        Unknown      -> "Unbekannt"
      )
  }

  type OtherSystems = FIGO :+: KDSSpread.Value :+: CNil

  
  implicit val format: OFormat[TumorStaging] =
    Json.format[TumorStaging]
}
