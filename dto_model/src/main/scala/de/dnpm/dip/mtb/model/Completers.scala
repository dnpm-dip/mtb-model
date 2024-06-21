package de.dnpm.dip.mtb.model


import java.time.LocalDate
import cats.{
  Applicative,
  Id
}
import de.dnpm.dip.util.{
  Completer,
  DisplayLabel
}
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
  CodeSystemProvider
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.icd.{
  ICD10GM,
  ICDO3
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.model.{
  History,
  Patient,
  Site,
  Reference,
}



trait Completers
{

  import scala.util.chaining._
  import Completer.syntax._


  protected implicit val hgnc: CodeSystemProvider[HGNC,Id,Applicative[Id]]

  protected implicit val atc: CodeSystemProvider[ATC,Id,Applicative[Id]]

  protected implicit val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]]

  protected implicit val icdo3: ICDO3.Catalogs[Id,Applicative[Id]]

  protected implicit val whoGrading: CodeSystemProvider[WHOGrading,Id,Applicative[Id]] = 
    new WHOGrading.Provider.Facade[Id]



  private implicit val patientCompleter: Completer[Patient] =
    Completer.of(
      pat =>
        pat.copy(
          gender       = pat.gender.complete,
          managingSite = Some(Site.local)
        )
    )


  private implicit def hgvsCompleter[S <: HGVS]: Completer[Coding[S]] =
    Completer.of(
      coding => coding.copy(
        display = coding.display.orElse(Some(coding.code.value))
      )
    )


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

/*    
    implicit val episodeCompleter: Completer[MTBEpisodeOfCare] =
      Completer.of(
        episode => episode.copy(
         status = episode.status.complete
        )
      )
*/
    implicit val diagnosisCompleter: Completer[MTBDiagnosis] =
      Completer.of(
        diagnosis => diagnosis.copy(
          code                     = diagnosis.code.complete, 
          whoGrading               = diagnosis.whoGrading.complete, 
          stageHistory             = diagnosis.stageHistory.map(_.map(st => st.copy(stage = st.stage.complete))), 
          guidelineTreatmentStatus = diagnosis.guidelineTreatmentStatus.complete,
          topography               = diagnosis.topography.complete
        )
      )


    implicit def indicationCompleter(
      implicit diagnoses: Seq[MTBDiagnosis]
    ): Completer[Reference[MTBDiagnosis]] =
      Completer.of(
        ref => ref.copy(
          display =
            ref.resolveOn(diagnoses)
              .map(_.code)
              .map(DisplayLabel.of(_).value)
        )
      )


    implicit def therapyCompleter(
      implicit diagnoses: Seq[MTBDiagnosis]
    ): Completer[MTBMedicationTherapy] =
      Completer.of {
        therapy =>
          therapy.copy(
            indication   = therapy.indication.complete,
            status       = therapy.status.complete,
            statusReason = therapy.statusReason.complete,
            medication   = therapy.medication.complete
          )
      }

    implicit def procedureCompleter(
      implicit diagnoses: Seq[MTBDiagnosis]
    ): Completer[OncoProcedure] =
      Completer.of {
        procedure => 
          procedure.copy(
            indication   = procedure.indication.complete,
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
          proteinExpressionResults = report.proteinExpressionResults.complete,
          msiMmrResults            = report.msiMmrResults.complete
        )
      )

    }



    implicit val snvCompleter: Completer[SNV] =
      Completer.of(
        snv => snv.copy(
          chromosome     = snv.chromosome.complete,
          gene           = snv.gene.complete,
          dnaChange      = snv.dnaChange.complete,
          proteinChange  = snv.proteinChange.complete,
          interpretation = snv.interpretation.complete
        )
      )

    implicit val cnvCompleter: Completer[CNV] =
      Completer.of(
        cnv => cnv.copy(
          chromosome            = cnv.chromosome.complete,
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
      diagnoses: Seq[MTBDiagnosis],
      variants: List[Variant]
    ): Completer[MTBMedicationRecommendation] =
      Completer.of(
        recommendation =>
          recommendation.copy(
            indication      = recommendation.indication.complete,
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
                  _.flatMap {
                    _.resolveOn(variants)
                     .map(
                       variant =>
                         Reference.to(variant)
                           .copy(display = Some(DisplayLabel.of(variant).value))
                     )
                  }
                )
          )
      )


    implicit def carePlanCompleter(
      implicit
      diagnoses: Seq[MTBDiagnosis],
      variants: List[Variant]
    ): Completer[MTBCarePlan] =
      Completer.of(
        carePlan => carePlan.copy(
          indication                      = carePlan.indication.complete,
          statusReason                    = carePlan.statusReason.complete,
          medicationRecommendations       = carePlan.medicationRecommendations.complete,
          geneticCounselingRecommendation = carePlan.geneticCounselingRecommendation.map(
            rec => rec.copy(reason = rec.reason.complete)
          )
        )
      )


    implicit def therapyHistoryCompleter(
      implicit diagnoses: Seq[MTBDiagnosis]
    ): Completer[History[MTBMedicationTherapy]] =
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
          record.diagnoses.complete.getOrElse(List.empty)


        record.copy(
        patient = record.patient.complete,
//        episodes = record.episodes.complete,
        diagnoses = Option(completedDiagnoses),
        guidelineMedicationTherapies = record.guidelineMedicationTherapies.complete,
        guidelineProcedures = record.guidelineProcedures.complete,
        performanceStatus = record.performanceStatus.complete,
        specimens = record.specimens.complete,
        histologyReports = record.histologyReports.complete,
        ihcReports = record.ihcReports.complete,
        ngsReports = record.ngsReports.complete,
        carePlans = record.carePlans.complete, 
        medicationTherapies = record.medicationTherapies.complete,
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
