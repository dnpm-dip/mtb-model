package de.dnpm.dip.mtb.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}
import de.dnpm.dip.model.{
  Id,
  Patient,
  Reference,
  DiagnosticReport,
  MolecularDiagnostics
}
import play.api.libs.json.{
  Json,
  OFormat
}


sealed trait Institute


final case class MolecularDiagnosticReport
(
  id: Id[MolecularDiagnosticReport],
  patient: Reference[Patient],
  performer: Option[Reference[Institute]],
  issuedOn: LocalDate,
  specimen: Reference[TumorSpecimen],
  `type`: Coding[MolecularDiagnosticReport.Type.Value],
  results: Option[List[String]]
)
extends DiagnosticReport
{
  override val notes = None
}

object MolecularDiagnosticReport
{

  object Type
  extends CodedEnum("dnpm-dip/mtb/molecular-diagnostics/type")
  with MolecularDiagnostics.Type
  with DefaultCodeSystem 
  {
    val FISH, PCR  = Value
    val GenePanel  = Value("gene-panel")
    val FusionPanel = Value("fusion-panel")

    override val display =
      Map( 
        Single          -> "Einzelgenanalyse",    
        FISH            -> "FISH",
        PCR             -> "PCR",
        GenePanel       -> "Genpanel",
        FusionPanel     -> "Fusionspanel",
      )
      .orElse(
        defaultDisplay
      )

  }


  implicit val format: OFormat[MolecularDiagnosticReport] = 
    Json.format[MolecularDiagnosticReport]

}
