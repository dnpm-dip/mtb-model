package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem,
}
import de.dnpm.dip.model.{
  Id,
  Period,
  Reference,
  Patient,
  Therapy,
  TherapyRecommendation,
  Procedure,
}
import play.api.libs.json.{
  Json,
  OFormat
}



final case class OncoProcedure
(
  id: Id[OncoProcedure],
  patient: Reference[Patient],
  indication: Reference[MTBDiagnosis],
  code: Coding[OncoProcedure.Type.Value],
  status: Coding[Therapy.Status.Value],
  statusReason: Option[Coding[Therapy.StatusReason]],
  therapyLine: Option[Int],
  basedOn: Option[Reference[TherapyRecommendation]],
  recordedOn: LocalDate,
  period: Option[Period[LocalDate]],
  note: Option[String]
)
extends Procedure[OncoProcedure.Type.Value]
{
  val category = None
}

object OncoProcedure
{

  object Type
  extends CodedEnum("mtb/procedure/type")
  with DefaultCodeSystem
  {
    val Surgery         = Value("surgery")
    val RadioTherapy    = Value("radio-therapy")
    val NuclearMedicine = Value("nuclear-medicine")

    override val display = {
      case Surgery         => "OP"
      case RadioTherapy    => "Strahlen-Therapie"
      case NuclearMedicine => "Nuklearmedizinische Therapie"
    }
  }


  implicit val format: OFormat[OncoProcedure] =
    Json.format[OncoProcedure]

}
