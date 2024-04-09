package de.dnpm.dip.mtb.model.v1.json


import java.time.{
  LocalDate,
  YearMonth
}
import java.time.temporal.Temporal
import scala.reflect.ClassTag
import scala.util.chaining._
import cats.data.NonEmptyList
import play.api.libs.json.JsObject
import json.{
  Json,
  Schema
}
import com.github.andyglow.jsonschema.CatsSupport._
import de.dnpm.dip.coding.{
  CodedEnum,
  Coding
}
import de.dnpm.dip.model.{
  Patient,
  Period,
  OpenEndPeriod,
  Reference,
}
import de.dnpm.dip.mtb.model.LevelOfEvidence
import de.dnpm.dip.mtb.model.v1._
import de.dnpm.dip.model.json.BaseSchemas

import shapeless.Witness
import com.github.andyglow.json.Value


trait Schemas extends BaseSchemas
{

  implicit val patientSchema: Schema[Patient] =
    Json.schema[Patient]
      .toSimpleNameDefinition


  implicit val consentSchema: Schema[Consent] = 
    Json.schema[Consent]
      .toSimpleNameDefinition


  implicit val diagnosisSchema: Schema[MTBDiagnosis] =
    Json.schema[MTBDiagnosis]
      .toSimpleNameDefinition


  implicit val episodeSchema: Schema[MTBEpisode] =
    Json.schema[MTBEpisode]
      .toSimpleNameDefinition


  implicit val medicationTherapySchema: Schema[MTBMedicationTherapy] =
    Json.schema[MTBMedicationTherapy]
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

  implicit val ngsReportSchema: Schema[NGSReport] =
    Json.schema[NGSReport]
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
