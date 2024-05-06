package de.dnpm.dip.mtb.model.json


import java.time.LocalDate
import java.time.temporal.Temporal
import scala.reflect.ClassTag
import cats.data.NonEmptyList
import play.api.libs.json.JsObject
import json.{
  Json,
  Schema
}
import com.github.andyglow.jsonschema.CatsSupport._
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.{
  Patient,
  Period,
  OpenEndPeriod,
  Reference,
}
import de.dnpm.dip.mtb.model._
import de.dnpm.dip.model.json.BaseSchemas



trait Schemas extends BaseSchemas
{

  implicit val patientSchema: Schema[Patient] =
    Json.schema[Patient]
      .toSimpleNameDefinition


  implicit val consentSchema: Schema[JsObject] = 
    Schema.`object`.Free[JsObject]()
      .toDefinition("Consent")


  implicit val diagnosisSchema: Schema[MTBDiagnosis] =
    Json.schema[MTBDiagnosis]
      .toSimpleNameDefinition


  implicit val episodeSchema: Schema[MTBEpisode] =
    Json.schema[MTBEpisode]
      .toSimpleNameDefinition


  implicit val medicationTherapySchema: Schema[MTBMedicationTherapy] =
    Json.schema[MTBMedicationTherapy]
      .toSimpleNameDefinition


  implicit val oncoProcedureSchema: Schema[OncoProcedure] =
    Json.schema[OncoProcedure]
      .toSimpleNameDefinition


  implicit val performanceStatusSchema: Schema[PerformanceStatus] =
    Json.schema[PerformanceStatus]
      .toSimpleNameDefinition


  implicit val tumorSpecimenSchema: Schema[TumorSpecimen] =
    Json.schema[TumorSpecimen]
      .toSimpleNameDefinition

  implicit val tumorCellContentSchema: Schema[TumorCellContent] =
    Json.schema[TumorCellContent]
      .toSimpleNameDefinition


  implicit val histologyReportSchema: Schema[HistologyReport] =
    Json.schema[HistologyReport]
      .toSimpleNameDefinition


  implicit val ihcReportSchema: Schema[IHCReport] =
    Json.schema[IHCReport]
      .toSimpleNameDefinition


  implicit val TMBSchema: Schema[TMB] =
    Json.schema[TMB]
      .toSimpleNameDefinition

  implicit val BRCAnessSchema: Schema[BRCAness] =
    Json.schema[BRCAness]
      .toSimpleNameDefinition

  implicit val HRDScoreSchema: Schema[HRDScore] =
    Json.schema[HRDScore]
      .toSimpleNameDefinition

  implicit val snvSchema: Schema[SNV] =
    Json.schema[SNV]
      .toSimpleNameDefinition

  implicit val cnvSchema: Schema[CNV] =
    Json.schema[CNV]
      .toSimpleNameDefinition

  implicit val dnaFusionSchema: Schema[DNAFusion] =
    Json.schema[DNAFusion]
      .toSimpleNameDefinition

  implicit val rnaFusionSchema: Schema[RNAFusion] =
    Json.schema[RNAFusion]
      .toSimpleNameDefinition

  implicit val rnaSeqSchema: Schema[RNASeq] =
    Json.schema[RNASeq]
      .toSimpleNameDefinition

  implicit val ngsReportSchema: Schema[SomaticNGSReport] =
    Json.schema[SomaticNGSReport]
      .toSimpleNameDefinition


  implicit val levelOfEvidenceSchema: Schema[LevelOfEvidence] =
    Json.schema[LevelOfEvidence]
      .toSimpleNameDefinition

  implicit val medicationRecommendationSchema: Schema[MTBMedicationRecommendation] =
    Json.schema[MTBMedicationRecommendation]
      .toSimpleNameDefinition

  implicit val geneticCounselingRecommendationSchema: Schema[GeneticCounselingRecommendation] =
    Json.schema[GeneticCounselingRecommendation]
      .toSimpleNameDefinition

  implicit val studyEnrollmentRecommendationSchema: Schema[StudyEnrollmentRecommendation] =
    Json.schema[StudyEnrollmentRecommendation]
      .toSimpleNameDefinition

  implicit val carePlanSchema: Schema[MTBCarePlan] =
    Json.schema[MTBCarePlan]
      .toSimpleNameDefinition


  implicit val claimSchema: Schema[Claim] =
    Json.schema[Claim]
      .toSimpleNameDefinition

  implicit val claimResponseSchema: Schema[ClaimResponse] =
    Json.schema[ClaimResponse]
      .toSimpleNameDefinition


  implicit val responseSchema: Schema[Response] =
    Json.schema[Response]
      .toSimpleNameDefinition


  implicit val patientRecordSchema: Schema[MTBPatientRecord] =
    Json.schema[MTBPatientRecord]

}

object Schemas extends Schemas
