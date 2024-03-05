package de.dnpm.dip.mtb.model


import java.time.LocalDate
import java.time.temporal.Temporal
import scala.reflect.ClassTag
import cats.data.NonEmptyList
import play.api.libs.json.JsObject
import json.{
  Json,
  Schema
}
import com.github.andyglow.json.Value
import com.github.andyglow.jsonschema.AsPlay._
import com.github.andyglow.jsonschema.CatsSupport._
import json.schema.Version._
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.{
  Patient,
  Period,
  OpenEndPeriod,
  Reference,
}
import de.dnpm.dip.model.json.BaseSchemas



trait MTBJsonSchemas extends BaseSchemas
{

  implicit val patientSchema: Schema[Patient] =
    Json.schema[Patient]


  implicit val consentSchema: Schema[JsObject] = 
    Schema.`object`.Free[JsObject]()


  implicit val diagnosisSchema: Schema[MTBDiagnosis] =
    Json.schema[MTBDiagnosis]


  implicit val episodeSchema: Schema[MTBEpisode] =
    Json.schema[MTBEpisode]


  implicit val medicationTherapySchema: Schema[MTBMedicationTherapy] =
    Json.schema[MTBMedicationTherapy]


  implicit val oncoProcedureSchema: Schema[OncoProcedure] =
    Json.schema[OncoProcedure]


  implicit val performanceStatusSchema: Schema[PerformanceStatus] =
    Json.schema[PerformanceStatus]


  implicit val tumorSpecimenSchema: Schema[TumorSpecimen] =
    Json.schema[TumorSpecimen]


  implicit val histologyReportSchema: Schema[HistologyReport] =
    Json.schema[HistologyReport]


  implicit val ihcReportSchema: Schema[IHCReport] =
    Json.schema[IHCReport]


  implicit val ngsReportSchema: Schema[NGSReport] =
    Json.schema[NGSReport]


  implicit val carePlanSchema: Schema[MTBCarePlan] =
    Json.schema[MTBCarePlan]


  implicit val claimSchema: Schema[Claim] =
    Json.schema[Claim]


  implicit val claimResponseSchema: Schema[ClaimResponse] =
    Json.schema[ClaimResponse]


  implicit val responseSchema: Schema[Response] =
    Json.schema[Response]


  implicit val episodesSchema: Schema[NonEmptyList[MTBEpisode]] =
    Json.schema[NonEmptyList[MTBEpisode]]


  implicit val patientRecordSchema: Schema[MTBPatientRecord] =
    Json.schema[MTBPatientRecord]

}

object MTBJsonSchemas extends MTBJsonSchemas
