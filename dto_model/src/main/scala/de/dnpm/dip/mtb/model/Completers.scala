package de.dnpm.dip.mtb.model


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
    Completer.of {
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
    }

  private implicit val icdo3mCompleter: Completer[Coding[ICDO3.M]] =
    Completer.of {
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
    }




  implicit val mtbPatientRecordCompleter: Completer[MTBPatientRecord] = {

    implicit val diagnosisCompleter: Completer[MTBDiagnosis] =
      Completer.of(
        diagnosis => diagnosis.copy(
          code                     = diagnosis.code.complete, 
//          whoGrading               = diagnosis.whoGrading.complete, 
//          stageHistory             = diagnosis.stageHistory.map(_.map(st => st.copy(stage = st.stage.complete))), 
          guidelineTreatmentStatus = diagnosis.guidelineTreatmentStatus.complete,
          topography               = diagnosis.topography.complete
        )
      )


    implicit def reasonCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[Reference[MTBDiagnosis]] =
      Completer.of(
        ref => 
          ref.resolve 
            .map(_.code)
            .map(DisplayLabel.of(_).value)
            .map(ref.withDisplay)
            .getOrElse(ref)
      )


    implicit def systemicTherapyCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[MTBSystemicTherapy] =
      Completer.of {
        therapy =>
          therapy.copy(
            reason       = therapy.reason.complete,
            status       = therapy.status.complete,
            statusReason = therapy.statusReason.complete,
            medication   = therapy.medication.complete
          )
      }

    implicit def procedureCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[OncoProcedure] =
      Completer.of {
        procedure => 
          procedure.copy(
            reason       = procedure.reason.complete,
            code         = procedure.code.complete,
            status       = procedure.status.complete,
            statusReason = procedure.statusReason.complete
        )
      }

    implicit val ecogCompleter: Completer[PerformanceStatus] =
      Completer.of(
        ecog => ecog.copy(value = ecog.value.complete)
      )

    implicit val specimenCompleter: Completer[TumorSpecimen] =
      Completer.of(
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
      )

    implicit val tumorCellContentompleter: Completer[TumorCellContent] =
      Completer.of(
        tcc => tcc.copy(
          method = tcc.method.complete
        )
      )
    implicit val histologyReportCompleter: Completer[HistologyReport] =
      Completer.of(
        report => report.copy(
          results = report.results.copy(
            tumorCellContent = report.results.tumorCellContent.complete,
            tumorMorphology  = report.results.tumorMorphology.map { obs => obs.copy(value = obs.value.complete) }
          )
        )
      )


    implicit val ihcReportCompleter: Completer[IHCReport] = {

      implicit val proteinExpressionCompleter: Completer[ProteinExpression] =
        Completer.of(
          obs => obs.copy(
            protein = obs.protein.complete,
            value   = obs.value.complete,  
            icScore = obs.icScore.complete,
            tcScore = obs.tcScore.complete
          )
        )

      Completer.of(
        report => report.copy(
          results = report.results.copy(
            proteinExpression = report.results.proteinExpression.complete,
            msiMmr            = report.results.msiMmr.complete
          )
        )
      )

    }



    implicit val snvCompleter: Completer[SNV] =
      Completer.of(
        snv => snv.copy(
//          chromosome     = snv.chromosome.complete,
          gene           = snv.gene.complete,
          dnaChange      = snv.dnaChange.complete,
          proteinChange  = snv.proteinChange.complete,
          interpretation = snv.interpretation.complete
        )
      )

    implicit val cnvCompleter: Completer[CNV] =
      Completer.of(
        cnv => cnv.copy(
//          chromosome            = cnv.chromosome.complete,
          `type`                = cnv.`type`.complete,
          reportedAffectedGenes = cnv.reportedAffectedGenes.complete,
          copyNumberNeutralLoH  = cnv.copyNumberNeutralLoH.complete,
        )
      )


    implicit val ngsReportCompleter: Completer[SomaticNGSReport] =
      Completer.of(
        report => report.copy(
          results = report.results.copy(
            tumorCellContent   = report.results.tumorCellContent.complete,
            tmb                = report.results.tmb.map(obs => obs.copy(interpretation = obs.interpretation.complete)),
            simpleVariants     = report.results.simpleVariants.complete,  
            copyNumberVariants = report.results.copyNumberVariants.complete,
            //TODO: Fusions, RNASeq
          )
        )
      )

    implicit def medicationRecommendationCompleter(
      implicit
      diagnoses: NonEmptyList[MTBDiagnosis],
      variants: List[Variant]
    ): Completer[MTBMedicationRecommendation] =
      Completer.of(
        recommendation =>
          recommendation.copy(
            reason       = recommendation.reason.complete,
            levelOfEvidence = recommendation.levelOfEvidence.map(
              loe => loe.copy(
                grading   = loe.grading.complete,
                addendums = loe.addendums.complete
              )
            ),
            priority        = recommendation.priority.complete,
            medication      = recommendation.medication.complete,
            supportingVariants =
              recommendation.supportingVariants
                .map(
                  _.map(ref => ref.withDisplay(DisplayLabel.of(ref).value))
                )
/*              
            supportingVariants =
              recommendation.supportingVariants
                .map(
                  _.flatMap {
                    _.resolve
                     .map(
                       variant =>
                         Reference.to(variant)
                           .copy(display = Some(DisplayLabel.of(variant).value))
                     )
                  }
                )
*/              
          )
      )


    implicit def carePlanCompleter(
      implicit
      diagnoses: NonEmptyList[MTBDiagnosis],
      variants: List[Variant]
    ): Completer[MTBCarePlan] =
      Completer.of(
        carePlan => carePlan.copy(
          reason                          = carePlan.reason.complete,
          statusReason                    = carePlan.statusReason.complete,
          medicationRecommendations       = carePlan.medicationRecommendations.complete,
          geneticCounselingRecommendation = carePlan.geneticCounselingRecommendation.map(
            rec => rec.copy(reason = rec.reason.complete)
          )
        )
      )


    implicit def therapyHistoryCompleter(
      implicit diagnoses: NonEmptyList[MTBDiagnosis]
    ): Completer[History[MTBSystemicTherapy]] =
      Completer.of(
        th => th.copy(
          history = th.history.complete
        )
        .ordered
      )


    implicit val responseCompleter: Completer[Response] =
      Completer.of(
        response => response.copy(
          value = response.value.complete
        )
      )


    Completer.of {
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
          histologyReports = record.histologyReports.complete,
          ihcReports = record.ihcReports.complete,
          ngsReports = record.ngsReports.complete,
          carePlans = record.carePlans.complete, 
          systemicTherapies = record.systemicTherapies.complete,
          responses = record.responses.complete, 
        )

    }

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
