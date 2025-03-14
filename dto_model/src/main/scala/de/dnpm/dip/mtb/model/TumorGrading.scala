package de.dnpm.dip.mtb.model


import java.time.LocalDate
import cats.data.NonEmptyList
import shapeless.{:+:, CNil}
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}
import play.api.libs.json.{
  Json,
  OFormat
}


final case class TumorGrading
(
  date: LocalDate,
  codes: NonEmptyList[Coding[TumorGrading.Systems]]
)


object OBDSGrading
extends CodedEnum("https://www.basisdatensatz.de/feld/161/grading")
with DefaultCodeSystem
{
  val Zero  = Value("0")
  val One   = Value("1")
  val Two   = Value("2")
  val Three = Value("3")
  val Four  = Value("4")
  val Five  = Value("5")
  val X, L, M, H, B, U, T = Value

  override val display =
    Map(
      Zero  -> "0 = primär erworbene Melanose ohne zelluläre Atypien (nur beim malignen Melanom der Konjunktiva)",
      One   -> "1 = gut differenziert",
      Two   -> "2 = mäßig differenziert",
      Three -> "3 = schlecht differenziert",
      Four  -> "4 = undifferenziert",
      Five  -> "5 = nur für C61, TNM8",
      X     -> "X = nicht bestimmbar",
      L     -> "L = low grade (G1 oder G2)",
      M     -> "M = intermediate grade (G2 oder G3)",
      H     -> "H = high grade (G3 oder G4)",
      B     -> "B = Borderline",
      U     -> "U = unbekannt",
      T     -> "T = trifft nicht zu",
    )
}


object TumorGrading
{

  type Systems = OBDSGrading.Value :+: WHOGrading :+: CNil

    // For Reads/Writes of NonEmptyList
  import de.dnpm.dip.util.json.{
    readsNel,
    writesNel
  }

  implicit val format: OFormat[TumorGrading] =
    Json.format[TumorGrading]
}

