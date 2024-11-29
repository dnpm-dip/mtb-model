package de.dnpm.dip.mtb.model.v1


import java.time.LocalDate
import java.util.UUID.randomUUID
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.UnregisteredMedication
import de.dnpm.dip.coding.hgnc.{
  HGNC,
  Ensembl
}
import de.dnpm.dip.model.{
  Id,
  ExternalId,
  History,
  Medications,
  NGSReport,
  Patient,
  Reference,
  Site,
  Study,
  Therapy,
}
import de.dnpm.dip.mtb.model
import model.{
  ClinVar,
  COSMIC,
  dbSNP,
  Entrez
}
import de.dnpm.dip.mtb.model.v1



package object mappings
{

  import de.dnpm.dip.util.mapping.syntax._


  // Source: https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Scala
  // See below for reason of usage
  private def levenshtein(s1: String, s2: String): Int = {

    import scala.collection.mutable

    val memorizedCosts = mutable.Map.empty[(Int, Int), Int]

    def lev: (Int, Int) => Int = {
      (k1, k2) =>
        memorizedCosts.getOrElseUpdate(
          (k1 -> k2),
          (k1 -> k2) match {
            case (i, 0) => i
            case (0, j) => j
            case (i, j) =>
              Seq(
                1 + lev(i - 1, j),
                1 + lev(i, j - 1),
                lev(i - 1, j - 1) + (if (s1(i - 1) != s2(j - 1)) 1 else 0)
              )
              .min
          }
        )
    }
    
    lev(s1.length, s2.length)
  }


  private implicit def idToId[T,U](id: Id[T]): Id[U] =
    id.asInstanceOf[Id[U]]


  private implicit def idToReference[T,U](id: Id[T]): Reference[U] =
    Reference.from(id)


  private implicit def enumValueToCoding[E <: Enumeration](
    v: E#Value
  )(
    implicit sys: CodeSystem[E#Value]
  ): Coding[E#Value] =
    Coding(v)


  implicit val anyToMedicationsCoding: Coding[Any] => Coding[Medications] = {

    val atc = "(?i)(atc)".r.unanchored

    coding =>
      coding.copy(
        system = coding.system.toString match {
          case atc(_) => Coding.System[ATC].uri
          case _      => Coding.System[UnregisteredMedication].uri
        }
      )
      .asInstanceOf[Coding[Medications]]
  }


  implicit val patientMapping: v1.Patient => Patient =
    patient =>
      Patient(
        patient.id,
        Coding(patient.gender),
        patient.birthDate.atDay(1), 
        patient.dateOfDeath.map(_.atEndOfMonth),
        Some(Site.local),
        patient.insurance.map(Reference.from(_)),
        None
      )


  implicit val episodeMapping: v1.MTBEpisode => model.MTBEpisodeOfCare =
    episode =>
      model.MTBEpisodeOfCare(
        episode.id,
        episode.patient,
        episode.period,
        None
      )


  implicit val diagnosisMapping: v1.MTBDiagnosis => model.MTBDiagnosis =
    diag =>
      model.MTBDiagnosis(
        diag.id,
        diag.patient,
        diag.recordedOn,
        diag.icd10,
        diag.icdO3T,
        None,
        diag.whoGrade,
        diag.statusHistory.map(
          _.map(
            st => model.MTBDiagnosis.TumorSpread(
              Coding(st.status),
              st.date
            )
          )
        ),
        diag.guidelineTreatmentStatus.map(Coding(_)),
      )


  implicit def medicationTherapyMapping(
    implicit
    @annotation.unused recommendations: List[v1.MTBMedicationRecommendation]
  ): v1.MTBMedicationTherapy => model.MTBMedicationTherapy =
    th =>
      model.MTBMedicationTherapy(
        th.id,
        th.patient,
        th.diagnosis.map(Reference.from(_)),
        th.therapyLine,
        th.basedOn
          .map(Reference.from(_)),
        th.recordedOn
          .orElse(th.period.flatMap(_.endOption))
          .getOrElse(LocalDate.now),  // TODO: seriously reconsider this fallback option to 'today'
        Coding(th.status.getOrElse(Therapy.Status.Unknown)),
        th.notDoneReason
          .orElse(th.reasonStopped),
        th.period,
        th.medication.map(_.mapAllTo[Coding[Medications]]),
        th.note
      )


  implicit val performanceStatusMapping: v1.PerformanceStatus => model.PerformanceStatus =
    p =>
      model.PerformanceStatus(
        p.id,
        p.patient,
        p.effectiveDate,
        p.value,
      )


  implicit val collectionMapping: v1.TumorSpecimen.Collection => model.TumorSpecimen.Collection =
    coll =>
      model.TumorSpecimen.Collection(
        coll.date,
        coll.method,
        coll.localization
      )

  implicit def specimenMapping(
    implicit diagnoses: List[v1.MTBDiagnosis]
  ): v1.TumorSpecimen => model.TumorSpecimen = {
    specimen =>

      model.TumorSpecimen(
        specimen.id,
        specimen.patient,
        // Levenshtein between ICD-10 codes of tumor specimen and diagnoses used here
        // because the code on tumor specimen (determined histologically) may be different
        // from the one documented on diagnosis, so resolve the code as a "reference"
        // to a diagnosis by picking the most similar one
        diagnoses
          .minBy(
            diag => levenshtein(diag.code.code.value,specimen.icd10.code.value)
          )
          .id
          .asInstanceOf[Id[model.MTBDiagnosis]],
        Coding(specimen.`type`.getOrElse(model.TumorSpecimen.Type.Unknown)),
        specimen.collection.map(_.mapTo[model.TumorSpecimen.Collection])
      )

  }


  implicit def tumorCellContentMapping(
    implicit patient: Reference[Patient]
  ): v1.TumorCellContent => model.TumorCellContent = 
    tcc =>
      model.TumorCellContent(
        tcc.id,
        patient,
        tcc.specimen,
        tcc.method,
        tcc.value
      )


  implicit val histologyReportMapping: v1.HistologyReport => model.HistologyReport = {
    report =>

      implicit val patient: Reference[Patient] =
        report.patient

      model.HistologyReport(
        report.id,
        report.patient,
        report.specimen,
        report.issuedOn,
        model.HistologyReport.Results(
          report.tumorMorphology.map(
            obs =>
              model.TumorMorphology(
                obs.id,
                obs.patient,
                obs.specimen,
                obs.value,
                obs.note
              )
          ),
          report.tumorCellContent.map(_.mapTo[model.TumorCellContent])
        )
      )
  }

  implicit val interpretationCodingToClinVar: Coding[Any] => Option[Coding[ClinVar.Value]] = {

    val likely     = "likely".r.unanchored
    val benign     = "benign".r.unanchored
    val pathogenic = "(onco|patho)genic".r.unanchored
    val uncertain  = "uncertain".r.unanchored

    coding =>
      Option(coding.code.value.toLowerCase)
        .collect {
          case "1"          => ClinVar.One
          case "2"          => ClinVar.Two
          case "3"          => ClinVar.Three
          case "4"          => ClinVar.Four
          case "5"          => ClinVar.Five
          case b @ benign() =>
            b match {
              case likely() => ClinVar.Two
              case _        => ClinVar.One
            }
          case p @ pathogenic(_) =>
            p match {
              case likely() => ClinVar.Four
              case _        => ClinVar.Five
            }
          case uncertain()  => ClinVar.Three
        }
      
  }


  implicit val sequencingTypeToEnum: String => Coding[NGSReport.SequencingType.Value] = {

    seqType => seqType.toLowerCase match {
      case "wes"   => NGSReport.SequencingType.Exome
      case "wgs"   => NGSReport.SequencingType.GenomeShortRead
      case "lrgs"  => NGSReport.SequencingType.GenomeLongRead
      case s       => NGSReport.SequencingType.withName(s)
    }

  }


  implicit def ngsReportMapping(
    implicit hgnc: CodeSystem[HGNC]
  ): v1.SomaticNGSReport => model.SomaticNGSReport = {

    import HGNC.extensions._

    implicit val geneCodingMapping: v1.GeneCoding => Coding[HGNC] =
      coding =>
        coding.hgncId
          .flatMap(hgnc.concept)
          .orElse(
            coding.ensemblId.flatMap(
              code => hgnc.concepts.find(_.ensemblID.exists(_ == code))
            )
          )
          .get
          .toCoding


    implicit def snvMapping(
      implicit patient: Reference[Patient]
    ): v1.SNV => model.SNV =
      snv =>
        model.SNV(
          snv.id ,
          patient,
          snv.cosmicId.map(id => ExternalId[model.SNV,COSMIC](id.value)).toSet ++
            snv.dbSNPId.map(id => ExternalId[model.SNV,dbSNP](id.value)),
          snv.chromosome,
          snv.gene.map(_.mapTo[Coding[HGNC]]),
          None,
          snv.startEnd,
          snv.altAllele,
          snv.refAllele,
          snv.dnaChange,
          snv.aminoAcidChange,
          snv.readDepth,
          snv.allelicFrequency,
          snv.interpretation.flatMap(_.mapTo[Option[Coding[ClinVar.Value]]])
        )


    implicit def cnvMapping(
      implicit patient: Reference[Patient]
    ): v1.CNV => model.CNV =
      cnv =>
        model.CNV(
          cnv.id,
          patient,
          cnv.chromosome,
          Some(cnv.startRange),
          Some(cnv.endRange),
          cnv.totalCopyNumber,
          cnv.relativeCopyNumber,
          cnv.cnA,
          cnv.cnB,
          cnv.reportedAffectedGenes.map(_.mapAllTo[Coding[HGNC]]),
          cnv.reportedFocality,
          cnv.`type`,
          cnv.copyNumberNeutralLoH.map(_.mapAllTo[Coding[HGNC]]),
        )


    implicit val dnaFusionPartnerMapping: v1.DNAFusion.Partner => model.DNAFusion.Partner =
      p => model.DNAFusion.Partner(
        p.chromosome,
        p.gene.mapTo[Coding[HGNC]],
        p.position
      )

    implicit def dnaFusionMapping(
      implicit patient: Reference[Patient]
    ): v1.DNAFusion => model.DNAFusion = 
      fusion =>
        model.DNAFusion(
          fusion.id,
          patient,
          fusion.fusionPartner5prime.mapTo[model.DNAFusion.Partner],
          fusion.fusionPartner3prime.mapTo[model.DNAFusion.Partner],
          fusion.reportedNumReads
        )


    implicit val rnaFusionPartnerMapping: v1.RNAFusion.Partner => model.RNAFusion.Partner =
      p => model.RNAFusion.Partner(
        Set(
          ExternalId[model.RNAFusion.Partner](p.transcriptId.value,None),
          ExternalId[model.RNAFusion.Partner](p.exon.value,None),
        ),
        p.gene.mapTo[Coding[HGNC]],
        p.position,
        p.strand
      )

    implicit def rnaFusionMapping(
      implicit patient: Reference[Patient]
    ): v1.RNAFusion => model.RNAFusion =
      fusion =>
        model.RNAFusion(
          fusion.id,
          patient,
          fusion.fusionPartner5prime.mapTo[model.RNAFusion.Partner],
          fusion.fusionPartner3prime.mapTo[model.RNAFusion.Partner],
          fusion.effect,
          fusion.cosmicId.map(id => ExternalId[model.RNAFusion,COSMIC](id.value)).toSet,
          fusion.reportedNumReads
        )


    implicit def rnaSeqMapping(
      implicit patient: Reference[Patient]
    ): v1.RNASeq => model.RNASeq =
      rnaSeq =>
        model.RNASeq(
          rnaSeq.id,
          patient,
          Set(
            ExternalId[model.RNASeq,Entrez](rnaSeq.entrezId.value),
            ExternalId[model.RNASeq,Ensembl](rnaSeq.ensemblId.value),
            ExternalId[model.RNASeq](rnaSeq.transcriptId.value,None)
          ),
          Some(rnaSeq.gene.mapTo[Coding[HGNC]]),
          model.RNASeq.Fragments(rnaSeq.fragmentsPerKilobaseMillion),
          rnaSeq.fromNGS,
          rnaSeq.tissueCorrectedExpression,
          rnaSeq.rawCounts,
          rnaSeq.librarySize,
          rnaSeq.cohortRanking,
        )


    implicit def tmbMapping(
      implicit
      patient: Reference[Patient],
      specimen: Reference[model.TumorSpecimen]
    ): Double => model.TMB =
      value =>
        model.TMB(
          Id[model.TMB](randomUUID.toString),
          patient,
          specimen,
          model.TMB.Result(value),
          None
        )


    implicit def brcanessMapping(
      implicit
      patient: Reference[Patient],
      specimen: Reference[model.TumorSpecimen]
    ): Double => model.BRCAness =
      value =>
        model.BRCAness(
          Id[model.BRCAness](randomUUID.toString),
          patient,
          specimen,
          value,
          model.BRCAness.referenceRange  //TODO: reconsider how to handle
        )


    report =>

      implicit val patient: Reference[Patient] =
        report.patient

      implicit val specimen: Reference[model.TumorSpecimen] =
        report.specimen

      model.SomaticNGSReport(
        report.id,
        patient,
        specimen,
        report.issueDate,
        report.sequencingType.mapTo[Coding[NGSReport.SequencingType.Value]],
        report.metadata,
        model.SomaticNGSReport.Results(
          report.tumorCellContent.map(_.mapTo[model.TumorCellContent]),
          report.tmb.map(_.mapTo[model.TMB]),
          report.brcaness.map(_.mapTo[model.BRCAness]),
          None,
          report.simpleVariants.map(_.mapAllTo[model.SNV]).getOrElse(List.empty),
          report.copyNumberVariants.map(_.mapAllTo[model.CNV]).getOrElse(List.empty),
          report.dnaFusions.map(_.mapAllTo[model.DNAFusion]).getOrElse(List.empty),
          report.rnaFusions.map(_.mapAllTo[model.RNAFusion]).getOrElse(List.empty),
          report.rnaSeqs.map(_.mapAllTo[model.RNASeq]).getOrElse(List.empty)
        )
      )
     
  }


  implicit def therapyRecommendationMapping(
    implicit date: LocalDate
  ): v1.MTBMedicationRecommendation => model.MTBMedicationRecommendation =
    rec =>
      model.MTBMedicationRecommendation(
        rec.id,
        Reference.from(rec.patient),
        Some(Reference.from(rec.diagnosis)),
        rec.issuedOn.getOrElse(date),
        rec.levelOfEvidence,
        rec.priority.map(Coding(_)),
        rec.medication.getOrElse(Set.empty).mapAllTo[Coding[Medications]],
        rec.supportingVariants
          .map(_.map(Reference.from(_)))
      )


  implicit def counselingRecommendationMapping(
    implicit date: LocalDate
  ): v1.GeneticCounselingRecommendation => model.GeneticCounselingRecommendation =
    rec =>
      model.GeneticCounselingRecommendation(
        rec.id,
        rec.patient,
        rec.issuedOn.getOrElse(date),
        Coding(model.GeneticCounselingRecommendation.Reason.Unknown)
      )


  implicit def studyEnrollmentRecommendationMapping(
    implicit date: LocalDate
  ): v1.StudyEnrollmentRecommendation => model.MTBStudyEnrollmentRecommendation =
    rec =>
      model.MTBStudyEnrollmentRecommendation(
        rec.id,
        rec.patient,
        rec.reason,
        rec.issuedOn.getOrElse(date),
        None,
        None,
        Some(
          List(
            ExternalId[Study](rec.nctNumber,None)
          )
        )
      )



  implicit def carePlanMapping(
    implicit 
    therapyRecommendations: List[v1.MTBMedicationRecommendation],
    geneticCounselingRecommendations: List[v1.GeneticCounselingRecommendation],
    studyInclusionRequests: List[v1.StudyEnrollmentRecommendation]
  ): v1.MTBCarePlan => model.MTBCarePlan = {
    cp =>

      implicit val issueDate =
        cp.issuedOn.getOrElse(LocalDate.now)

      model.MTBCarePlan(
        cp.id,
        cp.patient,
        Some(cp.diagnosis),
        issueDate,
        cp.noTargetFinding
          .map(
            _ => Coding(model.MTBCarePlan.StatusReason.NoTarget)
          ),
        cp.recommendations
          .map(
            _.flatMap(_.resolveOn(therapyRecommendations))
             .mapAllTo[model.MTBMedicationRecommendation]
          ),
        cp.geneticCounsellingRequest
          .flatMap(_.resolveOn(geneticCounselingRecommendations))
          .map(_.mapTo[model.GeneticCounselingRecommendation]),
        cp.studyInclusionRequests
          .map(
            _.flatMap(_.resolveOn(studyInclusionRequests))
             .mapAllTo[model.MTBStudyEnrollmentRecommendation]
           ),
        cp.description
      )
  }

  implicit val claimMapping: v1.Claim => model.Claim =
    claim =>
      model.Claim(
        claim.id,
        claim.patient,
        claim.therapy,
        claim.issuedOn,
        Coding(model.Claim.Stage.Unknown)
      )

  implicit val claimResponseMapping: v1.ClaimResponse => model.ClaimResponse =
    response =>
      model.ClaimResponse(
        response.id,
        response.patient,
        response.claim,
        response.issuedOn,
        Coding(response.status),
        response.reason.map(Coding(_))
      )


  implicit def therapyHistoryMapping(
    implicit
//    diagnoses: List[v1.MTBDiagnosis],
    recommendations: List[v1.MTBMedicationRecommendation]
  ): History[v1.MTBMedicationTherapy] => History[model.MTBMedicationTherapy] =
    h => h.copy(
      history = h.history.mapAllTo[model.MTBMedicationTherapy]
    )


  implicit val responseMapping: v1.Response => model.Response =
    response =>
      model.Response(
        response.id,
        response.patient,
        response.therapy,
        response.effectiveDate,
        response.value,
      )


  implicit def mtbPatientRecordMapping(
    implicit hgnc: CodeSystem[HGNC]
  ): v1.MTBPatientRecord => model.MTBPatientRecord = {
    record =>

    implicit val diagnoses =
      record.getDiagnoses

    implicit val therapyRecommendations =
      record.getRecommendations

    implicit val geneticCounselingRecommendations =
      record.getGeneticCounsellingRequests

    implicit val studyInclusionRequests =
      record.getStudyInclusionRequests


    model.MTBPatientRecord(
      record.patient.mapTo[Patient],
      NonEmptyList.one(record.episode.mapTo[model.MTBEpisodeOfCare]),
      Some(diagnoses.mapAllTo[model.MTBDiagnosis]),
      Some(
        (record.getPreviousGuidelineTherapies ++ record.getLastGuidelineTherapies)
          .mapAllTo[model.MTBMedicationTherapy]
      ),
      None,   // No OncoProcedures in V1 MTB model
      record.ecogStatus.map(_.mapAllTo[model.PerformanceStatus]),
      record.specimens.map(_.mapAllTo[model.TumorSpecimen]),
      record.histologyReports.map(_.mapAllTo[model.HistologyReport]),
      None,   // No IHC-Reports in V1 MTB model
      record.ngsReports.map(_.mapAllTo[model.SomaticNGSReport]),
      record.carePlans.map(_.mapAllTo[model.MTBCarePlan]),
      record.claims.map(_.mapAllTo[model.Claim]),
      record.claimResponses.map(_.mapAllTo[model.ClaimResponse]),
      record.molecularTherapies.map(_.mapAllTo[History[model.MTBMedicationTherapy]]),
      record.responses.map(_.mapAllTo[model.Response])
    )  
  }

}
