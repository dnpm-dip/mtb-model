package de.dnpm.dip.mtb.model


import cats.data.NonEmptyList
import de.dnpm.dip.model.{
  History,
  Patient,
  PatientRecord
}
import play.api.libs.json.{
  Json,
  JsObject,
  OFormat
}


final case class MTBPatientRecord
(
  patient: Patient,
  consent: JsObject,
  episodesOfCare: NonEmptyList[MTBEpisodeOfCare],
  diagnoses: Option[List[MTBDiagnosis]],
  guidelineMedicationTherapies: Option[List[MTBMedicationTherapy]],
  guidelineProcedures: Option[List[OncoProcedure]],
  performanceStatus: Option[List[PerformanceStatus]],
  specimens: Option[List[TumorSpecimen]],
  histologyReports: Option[List[HistologyReport]],
  ihcReports: Option[List[IHCReport]],
  ngsReports: Option[List[SomaticNGSReport]],
  carePlans: Option[List[MTBCarePlan]],
  claims: Option[List[Claim]],
  claimResponses: Option[List[ClaimResponse]],
  medicationTherapies: Option[List[History[MTBMedicationTherapy]]],
  responses: Option[List[Response]]
)
extends PatientRecord
{
  def getDiagnoses =
    diagnoses.getOrElse(List.empty)

  def getGuidelineMedicationTherapies =
    guidelineMedicationTherapies.getOrElse(List.empty)

  def getGuidelineProcedures =
    guidelineProcedures.getOrElse(List.empty)

  def getPerformanceStatus =
    performanceStatus.getOrElse(List.empty)

  def getSpecimens =
    specimens.getOrElse(List.empty)

  def getHistologyReports =
    histologyReports.getOrElse(List.empty)

  def getIhcReports =
    ihcReports.getOrElse(List.empty)

  def getNgsReports =
    ngsReports.getOrElse(List.empty)

  def getCarePlans =
    carePlans.getOrElse(List.empty)

  def getClaims =
    claims.getOrElse(List.empty)

  def getClaimResponses =
    claimResponses.getOrElse(List.empty)

  def getMedicationTherapies =
    medicationTherapies.getOrElse(List.empty)

  def getResponses =
    responses.getOrElse(List.empty)

}


object MTBPatientRecord
{
  import de.dnpm.dip.util.json._

  implicit val format: OFormat[MTBPatientRecord] =
    Json.format[MTBPatientRecord]

}
