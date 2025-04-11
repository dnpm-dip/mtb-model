package de.dnpm.dip.mtb.model


import scala.util.chaining._
import cats.{
  Applicative,
  Id
}
import cats.data.NonEmptyList
import de.dnpm.dip.util.{
  Completer,
  DisplayLabel
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystemProvider
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.icd.{
  ICD10GM,
  ICDO3
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.model.{
  BaseCompleters,
  History,
  Reference,
}



trait Completers extends BaseCompleters
{

  import Completer.syntax._


  protected implicit val hgnc: CodeSystemProvider[HGNC,Id,Applicative[Id]]

  protected implicit val atc: CodeSystemProvider[ATC,Id,Applicative[Id]]

  protected implicit val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]]

  protected implicit val icdo3: ICDO3.Catalogs[Id,Applicative[Id]]

  protected implicit val whoGrading: CodeSystemProvider[WHOGrading,Id,Applicative[Id]] = 
    new WHOGrading.Provider.Facade[Id]


  private implicit val icdo3tCompleter: Completer[Coding[ICDO3.T]] =
    coding =>
      coding.version
        .flatMap(icdo3.topography(_))
        .getOrElse(icdo3.topography)
        .concept(coding.code)
        .map(
          concept =>
            coding.copy(
              display = Some(concept.display),
              version = concept.version
            )
        )
        .getOrElse(coding)

  private implicit val icdo3mCompleter: Completer[Coding[ICDO3.M]] =
    coding =>
      coding.version
        .flatMap(icdo3.morphology(_))
        .getOrElse(icdo3.morphology)
        .concept(coding.code)
        .map(
          concept =>
            coding.copy(
              display = Some(concept.display),
              version = concept.version
            )
        )
        .getOrElse(coding)


  implicit val mtbPatientRecordCompleter: Completer[MTBPatientRecord] = {

    implicit val diagnosisCompleter: Completer[MTBDiagnosis] = {

      implicit val gradingCompleter: Completer[TumorGrading] =
        grading => grading.copy(
          codes = grading.codes.complete
        )

      implicit val stagingCompleter: Completer[TumorStaging] = {

        import TumorStaging.TNM.{AJCC,UICC}
        import TumorStaging.FIGO

        implicit val ajccCompleter: Completer[Coding[AJCC]] =
          Coding.completeDisplayWithCode

        implicit val uiccCompleter: Completer[Coding[UICC]] =
          Coding.completeDisplayWithCode

        implicit val figoCompleter: Completer[Coding[FIGO]] =
          Coding.completeDisplayWithCode

        staging => staging.copy(
          method = staging.method.complete,
          tnmClassification = staging.tnmClassification.map {
            case tnm @ TumorStaging.TNM(t,n,m) =>
              tnm.copy(
                tumor      = t.complete,
                nodes      = n.complete,
                metastasis = m.complete,
              )
          },
          otherClassifications = staging.otherClassifications.complete
        ) 
      }

      diagnosis => diagnosis.copy(
        code                     = diagnosis.code.complete, 
        germlineCodes            = diagnosis.germlineCodes.complete, 
        topography               = diagnosis.topography.complete,
        grading                  = diagnosis.grading.complete,
        staging                  = diagnosis.staging.complete, 
        guidelineTreatmentStatus = diagnosis.guidelineTreatmentStatus.complete,
      )

    }

    implicit def reasonCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[Reference[MTBDiagnosis]] =
      ref => 
        ref.resolve 
          .map(_.code)
          .map(DisplayLabel.of(_).value)
          .map(ref.withDisplay)
          .getOrElse(ref)


    implicit def systemicTherapyCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[MTBSystemicTherapy] =
      therapy =>
        therapy.copy(
          reason       = therapy.reason.complete,
          intent       = therapy.intent.complete,
          category     = therapy.category.complete,
          status       = therapy.status.complete,
          statusReason = therapy.statusReason.complete,
          recommendationFulfillmentStatus = therapy.recommendationFulfillmentStatus.complete,
          medication   = therapy.medication.complete
        )

    implicit def procedureCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[OncoProcedure] =
      procedure => 
        procedure.copy(
          reason       = procedure.reason.complete,
          intent       = procedure.intent.complete,
          code         = procedure.code.complete,
          status       = procedure.status.complete,
          statusReason = procedure.statusReason.complete
      )

    implicit val ecogCompleter: Completer[PerformanceStatus] =
      ecog => ecog.copy(value = ecog.value.complete)

    implicit val specimenCompleter: Completer[TumorSpecimen] =
      specimen => specimen.copy(
        `type` = specimen.`type`.complete,
        collection = specimen.collection.map(
          coll =>
            coll.copy(
              method       = coll.method.complete,
              localization = coll.localization.complete
            )
        )
      )

    implicit val molecularDiagnosticReportCompleter: Completer[MolecularDiagnosticReport] =
      report => report.copy(
        `type` = report.`type`.complete
      )

    implicit val tumorCellContentCompleter: Completer[TumorCellContent] =
      tcc => tcc.copy(
        method = tcc.method.complete
      )

    implicit val histologyReportCompleter: Completer[HistologyReport] =
      report => report.copy(
        results = report.results.copy(
          tumorMorphology  = report.results.tumorMorphology.pipe(obs => obs.copy(value = obs.value.complete)),
          tumorCellContent = report.results.tumorCellContent.complete
        )
      )


    implicit val ihcReportCompleter: Completer[IHCReport] = {

      implicit val proteinExpressionCompleter: Completer[ProteinExpression] =
        obs => obs.copy(
          protein = obs.protein.complete,
          value   = obs.value.complete,  
          icScore = obs.icScore.complete,
          tcScore = obs.tcScore.complete
        )

      report => report.copy(
        results = report.results.copy(
          proteinExpression = report.results.proteinExpression.complete,
          msiMmr            = report.results.msiMmr.complete
        )
      )

    }


    implicit val ngsReportCompleter: Completer[SomaticNGSReport] = {

      implicit val snvCompleter: Completer[SNV] =
        snv => snv.copy(
          gene           = snv.gene.complete,
          localization   = snv.localization.complete,
          interpretation = snv.interpretation.complete
        )
      
      implicit val cnvCompleter: Completer[CNV] =
        cnv => cnv.copy(
          `type`                = cnv.`type`.complete,
          localization          = cnv.localization.complete,
          reportedAffectedGenes = cnv.reportedAffectedGenes.complete,
          copyNumberNeutralLoH  = cnv.copyNumberNeutralLoH.complete,
        )

      implicit val dnaFusionCompleter: Completer[DNAFusion] = {

        implicit val partnerCompleter: Completer[DNAFusion.Partner] =
          p => p.copy(gene = p.gene.complete)

        fusion => fusion.copy(
          localization = fusion.localization.complete,
          fusionPartner5prime = fusion.fusionPartner5prime.complete,
          fusionPartner3prime = fusion.fusionPartner3prime.complete
        )
      }

      implicit val rnaFusionCompleter: Completer[RNAFusion] = {

        implicit val partnerCompleter: Completer[RNAFusion.Partner] =
          p => p.copy(gene = p.gene.complete)

        fusion => fusion.copy(
          localization = fusion.localization.complete,
          fusionPartner5prime = fusion.fusionPartner5prime.complete,
          fusionPartner3prime = fusion.fusionPartner3prime.complete
        )
      }

      implicit val rnaSeqCompleter: Completer[RNASeq] =
        rnaseq => rnaseq.copy(
          localization = rnaseq.localization.complete,
          gene         = rnaseq.gene.complete
        )

      report => report.copy(
        results = report.results.copy(
          tumorCellContent   = report.results.tumorCellContent.complete,
          tmb                = report.results.tmb.map(obs => obs.copy(interpretation = obs.interpretation.complete)),
          hrdScore           = report.results.hrdScore.map(obs => obs.copy(interpretation = obs.interpretation.complete)),
          simpleVariants     = report.results.simpleVariants.complete,  
          copyNumberVariants = report.results.copyNumberVariants.complete,
          dnaFusions         = report.results.dnaFusions.complete,
          rnaFusions         = report.results.rnaFusions.complete,
          rnaSeqs            = report.results.rnaSeqs.complete
        )
      )
    }


    implicit def carePlanCompleter(
      implicit
      diagnoses: NonEmptyList[MTBDiagnosis],
      variants: List[Variant]
    ): Completer[MTBCarePlan] = {

      implicit val loeCompleter: Completer[LevelOfEvidence] =
        loe => loe.copy(
          grading   = loe.grading.complete,
          addendums = loe.addendums.complete
        )

      implicit val medicationRecommendationCompleter: Completer[MTBMedicationRecommendation] =
        recommendation => recommendation.copy(
          reason          = recommendation.reason.complete,
          priority        = recommendation.priority.complete,
          levelOfEvidence = recommendation.levelOfEvidence.complete,
          category        = recommendation.category.complete,
          medication      = recommendation.medication.complete,
          useType         = recommendation.useType.complete,
          supportingVariants =
            recommendation.supportingVariants
              .map(
                _.map(ref => ref.withDisplay(DisplayLabel.of(ref).value))
              )
        )

      implicit val procedureRecommendationCompleter: Completer[MTBProcedureRecommendation] =
        recommendation => recommendation.copy(
          reason          = recommendation.reason.complete,
          priority        = recommendation.priority.complete,
          levelOfEvidence = recommendation.levelOfEvidence.complete,
          code            = recommendation.code.complete,
            supportingVariants =
              recommendation.supportingVariants
                .map(
                  _.map(ref => ref.withDisplay(DisplayLabel.of(ref).value))
                )
        )

      implicit val studyRecommendationCompleter: Completer[MTBStudyEnrollmentRecommendation] =
        recommendation => recommendation.copy(
          reason          = recommendation.reason.complete,
          priority        = recommendation.priority.complete,
          levelOfEvidence = recommendation.levelOfEvidence.complete,
          medication      = recommendation.medication.complete,
            supportingVariants =
              recommendation.supportingVariants
                .map(
                  _.map(ref => ref.withDisplay(DisplayLabel.of(ref).value))
                )
        )


      carePlan => carePlan.copy(
        reason                          = carePlan.reason.complete,
        statusReason                    = carePlan.statusReason.complete,
        geneticCounselingRecommendation = carePlan.geneticCounselingRecommendation.map(
          rec => rec.copy(reason = rec.reason.complete)
        ),
        medicationRecommendations       = carePlan.medicationRecommendations.complete,
        procedureRecommendations        = carePlan.procedureRecommendations.complete,
        studyEnrollmentRecommendations  = carePlan.studyEnrollmentRecommendations.complete,
      )

    }

    implicit val claimCompleter: Completer[Claim] =
      claim => claim.copy(
        requestedMedication = claim.requestedMedication.complete,
        stage               = claim.stage.complete
      )

    implicit val claimResponseCompleter: Completer[ClaimResponse] =
      cr => cr.copy(
        status       = cr.status.complete,
        statusReason = cr.statusReason.complete,
      )


    implicit def therapyHistoryCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[History[MTBSystemicTherapy]] =
      th => th.copy(
        history = th.history.complete
      )
      .ordered


    implicit val responseCompleter: Completer[Response] =
      response => response.copy(
        method = response.method.complete,
        value  = response.value.complete
      )


      record => 

        implicit val variants =
          record.getNgsReports.flatMap(_.variants)
  
        implicit val completedDiagnoses =
          record.diagnoses.complete

        record.copy(
          patient = record.patient.complete,
          diagnoses = completedDiagnoses,
          guidelineTherapies = record.guidelineTherapies.complete,
          guidelineProcedures = record.guidelineProcedures.complete,
          performanceStatus = record.performanceStatus.complete,
          specimens = record.specimens.complete,
          priorDiagnosticReports = record.priorDiagnosticReports.complete,
          histologyReports = record.histologyReports.complete,
          ihcReports = record.ihcReports.complete,
          ngsReports = record.ngsReports.complete,
          carePlans = record.carePlans.complete,
          followUps = record.followUps.complete,
          claims = record.claims.complete,
          claimResponses = record.claimResponses.complete,
          systemicTherapies = record.systemicTherapies.complete,
          responses = record.responses.complete, 
        )

  }

}


object Completers extends Completers
{

  override implicit lazy val hgnc: CodeSystemProvider[HGNC,Id,Applicative[Id]] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get


  override implicit lazy val atc: CodeSystemProvider[ATC,Id,Applicative[Id]] =
    ATC.Catalogs
      .getInstance[cats.Id]
      .get


  override implicit lazy val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]] =
    ICD10GM.Catalogs
      .getInstance[cats.Id]
      .get


  override implicit lazy val icdo3: ICDO3.Catalogs[Id,Applicative[Id]] =
    ICDO3.Catalogs
      .getInstance[cats.Id]
      .get

}
