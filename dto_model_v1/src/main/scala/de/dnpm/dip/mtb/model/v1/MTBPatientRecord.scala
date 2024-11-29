package de.dnpm.dip.mtb.model.v1


import play.api.libs.json.{
  Json,
  OFormat
}
import de.dnpm.dip.model.History


final case class MTBPatientRecord
(
  patient: Patient,
  consent: Consent,
  episode: MTBEpisode,
  diagnoses: Option[List[MTBDiagnosis]],
  previousGuidelineTherapies: Option[List[MTBMedicationTherapy]],
  lastGuidelineTherapies: Option[List[MTBMedicationTherapy]],
  ecogStatus: Option[List[PerformanceStatus]],
  specimens: Option[List[TumorSpecimen]],
//  molecularPathologyFindings: Option[List[MolecularPathologyFinding]]
  histologyReports: Option[List[HistologyReport]],
  ngsReports: Option[List[SomaticNGSReport]],
  carePlans: Option[List[MTBCarePlan]],
  recommendations: Option[List[MTBMedicationRecommendation]],
  geneticCounsellingRequests: Option[List[GeneticCounselingRecommendation]],
//  rebiopsyRequests: Option[List[RebiopsyRequest]]
//  histologyReevaluationRequests: Option[List[HistologyReevaluationRequest]],
  studyInclusionRequests: Option[List[StudyEnrollmentRecommendation]],
  claims: Option[List[Claim]],
  claimResponses: Option[List[ClaimResponse]],
  molecularTherapies: Option[List[History[MTBMedicationTherapy]]],
  responses: Option[List[Response]]
){

  def getDiagnoses =
    diagnoses.getOrElse(List.empty)

  def getPreviousGuidelineTherapies =
    previousGuidelineTherapies.getOrElse(List.empty)

  def getLastGuidelineTherapies =
    lastGuidelineTherapies.getOrElse(List.empty)

  def getEcogStatus =
    ecogStatus.getOrElse(List.empty)

  def getSpecimens =
    specimens.getOrElse(List.empty)

  def getHistologyReports =
    histologyReports.getOrElse(List.empty)

  def getNgsReports =
    ngsReports.getOrElse(List.empty)

  def getRecommendations =
    recommendations.getOrElse(List.empty)

  def getGeneticCounsellingRequests =
    geneticCounsellingRequests.getOrElse(List.empty)

  def getStudyInclusionRequests =
    studyInclusionRequests.getOrElse(List.empty)

  def getCarePlans =
    carePlans.getOrElse(List.empty)

  def getClaims =
    claims.getOrElse(List.empty)

  def getClaimResponses =
    claimResponses.getOrElse(List.empty)

  def getMolecularTherapies =
    molecularTherapies.getOrElse(List.empty)

  def getResponses =
    responses.getOrElse(List.empty)

}


object MTBPatientRecord
{
  implicit val format: OFormat[MTBPatientRecord] =
    Json.format[MTBPatientRecord]
}
