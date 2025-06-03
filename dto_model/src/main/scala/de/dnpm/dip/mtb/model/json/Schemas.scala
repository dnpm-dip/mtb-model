package de.dnpm.dip.mtb.model.json


import json.{
  Json,
  Schema
}
import com.github.andyglow.jsonschema.CatsSupport._ // For correct handling of NonEmptyList in Schema derivation
import de.dnpm.dip.model.{
  ExternalId
}
import de.dnpm.dip.model.json.BaseSchemas
import de.dnpm.dip.mtb.model._


trait Schemas extends BaseSchemas
{

  implicit val tumorGradingSchema: Schema[TumorGrading] =
    Json.schema[TumorGrading]
      .toSimpleNameDefinition

  implicit val tumorStagingSchema: Schema[TumorStaging] =
    Json.schema[TumorStaging]
      .toSimpleNameDefinition

  implicit val diagnosisTypeSchema: Schema[MTBDiagnosis.Type] =
    Json.schema[MTBDiagnosis.Type]
    

  implicit val diagnosisSchema: Schema[MTBDiagnosis] =
    Json.schema[MTBDiagnosis]
      .toSimpleNameDefinition


  implicit val episodeSchema: Schema[MTBEpisodeOfCare] =
    Json.schema[MTBEpisodeOfCare]
      .toSimpleNameDefinition


  implicit val medicationTherapySchema: Schema[MTBSystemicTherapy] =
    Json.schema[MTBSystemicTherapy]
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


  implicit val proteinExpressionSchema: Schema[ProteinExpression] =
    Json.schema[ProteinExpression]
      .toSimpleNameDefinition

  implicit val ihcReportSchema: Schema[IHCReport] =
    Json.schema[IHCReport]
      .toSimpleNameDefinition

  implicit val TMBResultSchema: Schema[TMB.Result] =
    Json.schema[TMB.Result]
      .addOptField("unit",Schema.`string`)
      .toDefinition("TMB-Result")

  implicit val TMBSchema: Schema[TMB] =
    Json.schema[TMB]
      .toSimpleNameDefinition

  implicit val BRCAnessSchema: Schema[BRCAness] =
    Json.schema[BRCAness]
      .toSimpleNameDefinition

  implicit val HRDScoreSchema: Schema[HRDScore] =
    Json.schema[HRDScore]
      .toSimpleNameDefinition

  implicit val seqMetadataSchema: Schema[SomaticNGSReport.Metadata] =
    Json.schema[SomaticNGSReport.Metadata]
      .toDefinition("NGSReport_Metadata")

  implicit def extVariantIdSchema[T <: Variant]: Schema[ExternalId[T,Variant.Systems]] =
    externalIdSchemaOf[T,Variant.Systems]("Variant_ExternalId")

  implicit val extTranscriptIdSchema: Schema[ExternalId[Transcript,Transcript.Systems]] =
    externalIdSchemaOf[Transcript,Transcript.Systems]("TranscriptId")


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

  implicit val studyEnrollmentRecommendationSchema: Schema[MTBStudyEnrollmentRecommendation] =
    Json.schema[MTBStudyEnrollmentRecommendation]
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
