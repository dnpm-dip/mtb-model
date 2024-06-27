package de.dnpm.dip.mtb.model


import de.dnpm.dip.coding.{
  CodedEnum,
  DefaultCodeSystem
}


object TumorGrade
extends CodedEnum("dnpm-dip/mtb/tumor-grade")
with DefaultCodeSystem
{
  val G1,G2,G3,G4,GX = Value

  override val display =
    Map(
      G1 -> "G1 – gut differenziert",
      G2 -> "G2 – mäßig differenziert",
      G3 -> "G3 – schlecht differenziert",
      G4 -> "G4 – nicht differenziert (sehr bösartig)",
      GX -> "Nicht ermittelbar"
    )
}


