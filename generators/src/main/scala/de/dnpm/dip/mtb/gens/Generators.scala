package de.dnpm.dip.mtb.gens



import java.net.URI
import java.time.LocalDate
import java.time.temporal.ChronoUnit.YEARS
import cats.data.NonEmptyList
import de.ekut.tbi.generators.Gen
import de.ekut.tbi.generators.DateTimeGens._
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.atc.Kinds.Substance
import de.dnpm.dip.coding.icd.{
  ICD10GM,
  ICD10GMCatalogs,
  ICDO3
}
import de.dnpm.dip.model.{
  Id,
  Episode,
  ExternalId,
  Reference,
  ExternalReference,
  Gender,
  Patient,
  Period,
  Organization,
  GuidelineTreatmentStatus,
  Therapy
}
import de.dnpm.dip.mtb.model._



trait Generators
{

  import MTBMedicationTherapy.statusReasonCodeSystem


  implicit def genId[T]: Gen[Id[T]] =
    Gen.uuidStrings
      .map(Id(_))

  implicit def genReference[T]: Gen[Reference[T]] =
    Gen.uuidStrings
      .map(Reference.id(_))

  implicit def genExternalId[T]: Gen[ExternalId[T]] =
    Gen.uuidStrings
      .map(ExternalId(_,None))


  implicit def genCodingfromCodeSystem[S: Coding.System: CodeSystem]: Gen[Coding[S]] =
    Gen.oneOf(CodeSystem[S].concepts)
      .map(_.toCoding)


  implicit val genPatient: Gen[Patient] =
    for {
      id <-
        Gen.of[Id[Patient]]

      gender <-
        Gen.of[Coding[Gender.Value]]

      birthDate <-
        localDatesBetween(
          LocalDate.now.minusYears(70),
          LocalDate.now.minusYears(30)
        )

      age =
        YEARS.between(birthDate,LocalDate.now)

      dateOfDeath <-
        Gen.option(
          Gen.longsBetween(age - 20L, age - 5L)
            .map(birthDate.plusYears),
          0.4
        )

      healthInsurance =
        ExternalReference[Organization](
          ExternalId("aok-ik","IK"),
          Some("AOK")
        )

    } yield
      Patient(
        id,
        gender,
        birthDate,
        dateOfDeath,
        None,
        Some(healthInsurance)
      )


  implicit lazy val icd10gm: CodeSystem[ICD10GM] =
    ICD10GMCatalogs
      .getInstance[cats.Id]
      .get
      .latest
      .filter(_.code.value startsWith "C")

  lazy val icdo3Topography: CodeSystem[ICDO3.Topography] =
    ICDO3.Catalogs
      .getInstance[cats.Id]
      .get
      .topography

  implicit val whoGradingSystem: CodeSystem[WHOGrading] =
    WHOGrading.codeSystem5th


  implicit val genDiagnosis: Gen[MTBDiagnosis] =
    for {
      id <- Gen.of[Id[MTBDiagnosis]]

      patient <- Gen.of[Id[Patient]]

      icd10 <- Gen.of[Coding[ICD10GM]]

      icdo3 =
        icdo3Topography
          .concepts
          .find(_.code.value == icd10.code.value)
          .map(_.toCoding)

      who <- Gen.of[Coding[WHOGrading]]

      stageHistory <-
        Gen.of[Coding[MTBDiagnosis.TumorStage.Value]]
          .map(MTBDiagnosis.StageOnDate(_,LocalDate.now))
          .map(Seq(_))

      gl <- Gen.of[Coding[GuidelineTreatmentStatus.Value]]

    } yield MTBDiagnosis(
      id,
      Reference(patient,None),
      Some(LocalDate.now),
      icd10,
      icdo3,
      Some(who),
      stageHistory,
      Some(gl)
    )


  implicit val genMTBEpisode: Gen[MTBEpisode] =
    for {
      id <- Gen.of[Id[MTBEpisode]]

      patient <- Gen.of[Reference[Patient]]

      period = Period(LocalDate.now.minusMonths(6))
     
      status <- Gen.of[Coding[Episode.Status.Value]]

      diagnoses <- 
        Gen.of[Reference[MTBDiagnosis]]
          .map(List(_))

    } yield MTBEpisode(
      id,
      patient,
      period,
      status,
      diagnoses
    )


  implicit val genPerformanceStatus: Gen[PerformanceStatus] =
    for {
      id <- Gen.of[Id[PerformanceStatus]]
      patient <- Gen.of[Reference[Patient]]
      value <- Gen.of[Coding[ECOG.Value]]
    } yield PerformanceStatus(
      id,
      patient,
      LocalDate.now,
      value
    )


  implicit lazy val atc: CodeSystem[ATC] =
    ATC.Catalogs
      .getInstance[cats.Id]
      .get
      .latest
      .filter(_.code.value startsWith "L01XX")
      .filter(ATC.filterByKind(Substance))

  val genGuidelineTherapy: Gen[MTBMedicationTherapy] = {

    import Therapy.Status._

    for {
      id <- Gen.of[Id[MTBMedicationTherapy]]

      patient <- Gen.of[Reference[Patient]]

      indication <- Gen.of[Reference[MTBDiagnosis]]

      therapyLine <- Gen.intsBetween(1,9)

//      recommendation <- Gen.of[Reference[MTBMedicationRecommendation]]

      status <- Gen.of[Coding[Therapy.Status.Value]]

      statusReason <- Gen.of[Codinig[Therapy.StatusReason]]
/*      
      statusReason <- 
        status match {
          case NotDone => Gen.oneOf()
        } 
*/

      period = Period(LocalDate.now.minusMonths(6))

      medication <-
        Gen.of[Coding[ATC]]
          .map(Set(_))

      note = "Notes on the therapy..."

    } yield MTBMedicationTherapy(
      id,
      patient,
      indication,
      Some(therapyLine),
      None,
      Some(LocalDate.now),
      status,
      Some(statusReason),
      Some(period),
      Some(medication),
      Some(note)
    )

  }


/*
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
  recordedOn: Option[LocalDate],
  period: Option[Period[LocalDate]],
  note: Option[String]
)
*/

  implicit val genPatientRecord: Gen[MTBPatientRecord] =
    for {

      patient <- Gen.of[Patient]

      patRef = Reference(patient)

      diagnosis <-
        Gen.of[MTBDiagnosis]
          .map(_.copy(patient = patRef))

      episode <-
        Gen.of[MTBEpisode]
          .map(
            _.copy(
              patient = patRef,
              diagnoses = List(Reference(diagnosis))
            )
          )

      performanceStatus <-
        Gen.of[PerformanceStatus] 
          .map(_.copy(patient = patRef))

      guidelineTherapies <-
        Gen.list(
          Gen.intsBetween(1,3),
          genGuidelineTherapy
        )

    } yield MTBPatientRecord(
      patient,
      List(episode),
      List(diagnosis),
      guidelineTherapies,
      List.empty,
      List(performanceStatus),
      List.empty,
      List.empty,
      List.empty,
      List.empty,
      List.empty,
      List.empty,
    )

}
