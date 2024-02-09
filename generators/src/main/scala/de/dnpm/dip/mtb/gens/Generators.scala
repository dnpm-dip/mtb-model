package de.dnpm.dip.mtb.gens



import java.net.URI
import java.time.LocalDate
import java.time.temporal.ChronoUnit.{
  MONTHS,
  YEARS
}
import scala.util.matching.Regex
import cats.data.NonEmptyList
import de.ekut.tbi.generators.Gen
import de.ekut.tbi.generators.DateTimeGens._
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
}
import de.dnpm.dip.coding.hgnc.{
  HGNC,
  Ensembl
}
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.atc.Kinds.Substance
import de.dnpm.dip.coding.icd.{
  ICD10GM,
  ICDO3,
  ClassKinds
}
import ClassKinds.Category
import de.dnpm.dip.coding.icd.ICD.extensions._
import de.dnpm.dip.model.{
  Id,
  ClosedInterval,
  Episode,
  ExternalId,
  Reference,
  ExternalReference,
  Gender,
  Patient,
  Period,
  Organization,
  GuidelineTreatmentStatus,
  Therapy,
  TherapyRecommendation
}
import de.dnpm.dip.mtb.model._
import MTBMedicationTherapy.StatusReason.{
  PaymentRefused,
  Progression
}



trait Generators
{

  private implicit val therapyStatusReason: CodeSystem[Therapy.StatusReason] =
    MTBMedicationTherapy.StatusReason.codeSystem


  private val oncoCode =
    """C(2|5|6|7)\d.\d""".r

  private implicit lazy val icd10gm: CodeSystem[ICD10GM] =
    ICD10GM.Catalogs
      .getInstance[cats.Id]
      .get
      .latest
      .filter(c => oncoCode matches c.code.value)


  private lazy val icdo3Topography: CodeSystem[ICDO3.Topography] =
    ICDO3.Catalogs
      .getInstance[cats.Id]
      .get
      .topography


  private implicit lazy val icdo3Morphology: CodeSystem[ICDO3.Morphology] =
    ICDO3.Catalogs
      .getInstance[cats.Id]
      .get
      .morphology
      .filter(_.classKind == Category)


  private implicit val whoGradingSystem: CodeSystem[WHOGrading] =
    WHOGrading.codeSystem5th

  private val atcRegex =
    """L01(EG|XA|XX|EN|EX|FX)""".r.unanchored

  private implicit lazy val atc: CodeSystem[ATC] =
    ATC.Catalogs
      .getInstance[cats.Id]
      .get
      .latest
      .filter(c => atcRegex matches c.code.value)
      .filter(ATC.filterByKind(Substance))

  private val symbols =
    Set(
      "ABL1",
      "AKT1",
      "BRCA1",
      "BRAF",
      "FGFR2",
      "FGFR3",
      "HRAS",
      "KRAS",
      "MDM2",
      "TP53",
    )


  private implicit lazy val hgnc: CodeSystem[HGNC] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get
      .latest
      .filter(gene => symbols contains gene.display)



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


  private val genGender: Gen[Coding[Gender.Value]] =
    Gen.distribution(
      48.0 -> Gender.Male,
      48.0 -> Gender.Female,
      2.0  -> Gender.Other,
    )
    .map(Coding(_))


  implicit val genPatient: Gen[Patient] =
    for {
      id <-
        Gen.of[Id[Patient]]

      gender <- genGender

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


  def genDiagnosis(
    patient: Patient
  ): Gen[MTBDiagnosis] =
    for {
      id <- Gen.of[Id[MTBDiagnosis]]

      icd10 <- Gen.of[Coding[ICD10GM]]

      date <-
        patient.dateOfDeath match {
          case Some(dod) =>
            for {
              os <- Gen.longsBetween(24,48)
            } yield dod.minusMonths(os)

          case _ => 
            val age =
              patient.ageIn(MONTHS).value.toLong
            for {
              onsetAge <- Gen.longsBetween(age - 48L, age - 24L)
            } yield patient.birthDate.plusMonths(onsetAge)
        }

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
      Reference(patient),
      Some(date),
      icd10,
      icdo3,
      Some(who),
      stageHistory,
      Some(gl)
    )


  def genMTBEpisode(
    patient: Reference[Patient],
    diagnoses: List[Reference[MTBDiagnosis]]
  ): Gen[MTBEpisode] =
    for {
      id <- Gen.of[Id[MTBEpisode]]

      period = Period(LocalDate.now.minusMonths(6))
     
      status <- Gen.of[Coding[Episode.Status.Value]]
     
    } yield MTBEpisode(
      id,
      patient,
      period,
      status,
      diagnoses
    )


  def genPerformanceStatus(
    patient: Reference[Patient]
  ): Gen[PerformanceStatus] =
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


  import Therapy.Status._

  def genGuidelineTherapy(
    patient: Reference[Patient],
    diagnosis: Reference[MTBDiagnosis],
  ): Gen[MTBMedicationTherapy] =
    for {
      id <-
        Gen.of[Id[MTBMedicationTherapy]]

      therapyLine <-
        Gen.intsBetween(1,9)

      status =
        Coding(Stopped)

      statusReason =
        Coding[Therapy.StatusReason](Progression)

      period <- 
        for {
          monthsAgo <- Gen.longsBetween(12,24)
          duration  <- Gen.longsBetween(8,36)
          start     =  LocalDate.now.minusMonths(monthsAgo)
          end       =  start.plusWeeks(duration)
        } yield Period(start,end)

      medication <-
        Gen.of[Coding[ATC]]
          .map(Set(_))

      note = "Notes on the therapy..."

    } yield MTBMedicationTherapy(
      id,
      patient,
      diagnosis,
      Some(therapyLine),
      None,
      Some(LocalDate.now),
      status,
      Some(statusReason),
      Some(period),
      Some(medication),
      Some(note)
    )


  def genProcedure(
    patient: Reference[Patient],
    diagnosis: Reference[MTBDiagnosis],
  ): Gen[OncoProcedure] =
    for { 
      id <- Gen.of[Id[OncoProcedure]]

      code <- Gen.of[Coding[OncoProcedure.Type.Value]]

      status <- Gen.of[Coding[Therapy.Status.Value]]

      statusReason <- Gen.of[Coding[Therapy.StatusReason]]

      therapyLine <- Gen.intsBetween(1,9)

      period = Period(LocalDate.now.minusMonths(6))

      note = "Notes on the therapy..."
    } yield OncoProcedure(
      id,
      patient,
      diagnosis,
      code,
      status,
      Some(statusReason),
      Some(therapyLine),
      None,
      Some(LocalDate.now),
      Some(period),
      Some(note)
    )


  def genTumorSpecimen(
    patient: Reference[Patient],
    diag: MTBDiagnosis
  ): Gen[TumorSpecimen] =
    for {
      id <- Gen.of[Id[TumorSpecimen]]

      typ <- Gen.of[Coding[TumorSpecimen.Type.Value]]

      method <- Gen.of[Coding[TumorSpecimen.Collection.Method.Value]]

      localization <- Gen.of[Coding[TumorSpecimen.Collection.Localization.Value]]

    } yield TumorSpecimen(
      id,
      patient,
      Reference(diag),
      typ,
      Some(
        TumorSpecimen.Collection(
          LocalDate.now,
          method,
          localization
        )
      )
    )


  def genTumorCellContent(
    patient: Reference[Patient],
    specimen: Reference[TumorSpecimen]
  ): Gen[TumorCellContent] = 
    for {
      obsId  <- Gen.of[Id[TumorCellContent]]
      method <- Gen.of[Coding[TumorCellContent.Method.Value]]
      value  <- Gen.doubles
    } yield TumorCellContent(
      obsId,
      patient,
      specimen,
      method,
      value
    )


  def genHistologyReport(
    patient: Reference[Patient],
    specimen: Reference[TumorSpecimen]
  ): Gen[HistologyReport] =
    for {
      id <- Gen.of[Id[HistologyReport]]

      morphology <- 
        for {
          obsId <- Gen.of[Id[TumorMorphology]]
          value <- Gen.of[Coding[ICDO3.Morphology]]
        } yield TumorMorphology(
          obsId,
          patient,
          specimen,
          value,
          Some("Notes...")
        )
                  
      tumorCellContent <- 
        genTumorCellContent(patient,specimen)

    } yield HistologyReport(
      id,
      patient,
      specimen,
      LocalDate.now,
      HistologyReport.Results(
        Some(morphology),
        Some(tumorCellContent.copy(method = Coding(TumorCellContent.Method.Histologic)))
      )
    )


  def genProteinExpression(
    patient: Reference[Patient],
  ): Gen[ProteinExpression] =
    for {
      id      <- Gen.of[Id[ProteinExpression]]
      protein <- Gen.of[Coding[HGNC]]
      value   <- Gen.of[Coding[ProteinExpression.Result.Value]]
      tpsScore <- Gen.intsBetween(0,100)
      icScore <- Gen.of[Coding[ProteinExpression.ICScore.Value]]
      tcScore <- Gen.of[Coding[ProteinExpression.TCScore.Value]]
    } yield ProteinExpression(
      id,
      patient,
      protein,
      value,
      Some(tpsScore),
      Some(icScore),
      Some(tcScore),
    )

  def genIHCReport(
    patient: Reference[Patient],
    specimen: Reference[TumorSpecimen]
  ): Gen[IHCReport] =
    for {
      id <- Gen.of[Id[IHCReport]]

      journalId <- Gen.of[ExternalId[Nothing]]

      blockId <- Gen.of[ExternalId[Nothing]]

      proteinExpression <-
        Gen.list(
          Gen.intsBetween(3,10),
          genProteinExpression(patient)
        )

    } yield IHCReport(
      id,
      patient,
      specimen,
      LocalDate.now,
      journalId,
      blockId,
      proteinExpression,
      List.empty,
    )



  private val bases = 
    Seq("A","C","G","T")

/*
  private val aminoAcids = 
    Seq(
      "Ala", "Asx", "Cys", "Asp",
      "Glu", "Phe", "Gly", "His",
      "Ile", "Lys", "Leu", "Met",
      "Asn", "Pro", "Gln", "Arg",
      "Ser", "Thr", "Sec", "Val",
      "Trp", "Tyr", "Glx"
     )
*/

  // Source: https://hgvs-nomenclature.org
  private val proteinChanges =
    Seq(
      "p.Gly12Cys",
      "p.Trp24Cys",
      "p.(Gly56Ala^Ser^Cys)",
      "p.Trp24=/Cys",
      "p.Cys28delinsTrpVal",
      "p.Cys28_Lys29delinsTrp",
      "p.(Pro578_Lys579delinsLeuTer)",
      "p.(Glu125_Ala132delinsGlyLeuHisArgPheIleValLeu)",
      "p.His4_Gln5insAla",
      "p.Lys2_Gly3insGlnSerLys",
      "p.Arg78_Gly79insX[23]",
      "p.Val7del",
      "p.Lys23_Val25del",
      "p.(His321Leufs*3)",
      "p.Gly2_Met46del",
    )


  def genSNV(patient: Reference[Patient]): Gen[SNV] =
    for { 
      id <-
        Gen.of[Id[Variant]]

      dbSnpId <-
        Gen.uuidStrings
          .map(ExternalId[SNV](_,Some(Coding.System[dbSNP].uri)))

      cosmicId <-
        Gen.uuidStrings
          .map(ExternalId[SNV](_,Some(Coding.System[COSMIC].uri)))
      
      chr <-
        Gen.of[Coding[Chromosome.Value]]

      gene <-
        Gen.of[Coding[HGNC]]

      transcriptId <-
        Gen.uuidStrings
          .map(ExternalId[Transcript,Ensembl](_))
//          .map(ExternalId[Transcript](_,Some(Coding.System[Ensembl].uri)))

      position <-
        Gen.longsBetween(24,600)

      ref <- Gen.oneOf(bases)    

      alt <- Gen.oneOf(bases filter (_ != ref))

      dnaChg = Coding[HGVS](s"c.$position$ref>$alt")

      proteinChg <- Gen.oneOf(proteinChanges).map(Coding[HGVS](_))

      readDepth <- Gen.intsBetween(5,25).map(SNV.ReadDepth(_))

      allelicFreq <- Gen.doubles.map(SNV.AllelicFrequency(_))

      interpretation <- Gen.of[Coding[ClinVar]]

    } yield SNV(
      id,
      patient,
      Set(dbSnpId,cosmicId),
      chr,
      Some(gene),
      Some(transcriptId),
      Variant.PositionRange(position,None),
      SNV.Allele(alt),
      SNV.Allele(ref),
      Some(dnaChg),
      Some(proteinChg),
      readDepth,
      allelicFreq,
      Some(interpretation)
    )


  def genCNV(patient: Reference[Patient]): Gen[CNV] =
    for { 
      id <- Gen.of[Id[Variant]]
      
      chr <- Gen.of[Coding[Chromosome.Value]]

      startRange <- Gen.longsBetween(42L,50000L).map(start => Variant.PositionRange(start,Some(start+42L)))

      length <- Gen.longsBetween(42L,1000L)

      endRange = Variant.PositionRange(startRange.start + length,Some(startRange.start + length + 50L))

      copyNum <- Gen.intsBetween(1,8)

      relCopyNum <- Gen.doubles

      cnA <- Gen.doubles

      cnB <- Gen.doubles

      affectedGenes <- Gen.list(Gen.intsBetween(1,4),Gen.of[Coding[HGNC]])

      focality = "partial q-arm"

      copyNumberNeutralLoH <- Gen.list(Gen.intsBetween(1,4),Gen.of[Coding[HGNC]])

      typ = copyNum match { 
        case n if n < 2 => CNV.Type.Loss
        case n if n < 4 => CNV.Type.LowLevelGain
        case n          => CNV.Type.HighLevelGain

      }

    } yield CNV(
      id,
      patient,
      chr,
      Some(startRange),
      Some(endRange),
      Some(copyNum),
      Some(relCopyNum),
      Some(cnA),
      Some(cnB),
      affectedGenes.distinctBy(_.code).toSet,
      Some(focality),
      Coding(typ),
      copyNumberNeutralLoH.distinctBy(_.code).toSet
    )


  def genNGSReport(
    patient: Reference[Patient],
    specimen: Reference[TumorSpecimen]
  ): Gen[NGSReport] =
    for {
      id <- Gen.of[Id[NGSReport]]

      seqType <-
        Gen.oneOf("tNGS","WES","WGS","Panel")

      metadata <-
        for {
          refGenome <- Gen.oneOf("HG19","HG38","GRCh37")
        } yield NGSReport.Metadata(
          "Kit Type",
          "Manufacturer",
          "Sequencer",
          refGenome,
          URI.create("https://github.com/pipeline-project")
        )

      tumorCellContent <- 
        genTumorCellContent(patient,specimen)

      tmb <-
        for {
          id <- Gen.of[Id[TMB]]
          value <- Gen.intsBetween(0,500000)
          interpretation <- Gen.of[Coding[TMB.Interpretation.Value]]
        } yield TMB(
          id,
          patient,
          specimen,
          TMB.Result(value),
          Some(interpretation)
        )

      brcaness <-
        for {
          id <- Gen.of[Id[BRCAness]]
        } yield BRCAness(
          id,
          patient,
          specimen,
          0.5,
          ClosedInterval(0.4,0.6)
        )

      hrdScore <-
        for {
          id <- Gen.of[Id[HRDScore]]
          value <- Gen.doubles
          lst <- Gen.doubles.map(HRDScore.LST(_))
          loh <- Gen.doubles.map(HRDScore.LoH(_))
          tai <- Gen.doubles.map(HRDScore.TAI(_))
          interpretation <- Gen.of[Coding[HRDScore.Interpretation.Value]]
        } yield HRDScore(
          id,
          patient,
          specimen,
          value,
          HRDScore.Components(
            lst,
            loh,
            tai
          ),
          Some(interpretation)
        )

      snvs <-
        Gen.list(
          Gen.intsBetween(4,10),
          genSNV(patient)
        )  

      cnvs <-
        Gen.list(
          Gen.intsBetween(4,10),
          genCNV(patient)
        )  

    } yield NGSReport(
      id,
      patient,
      specimen,
      LocalDate.now,
      seqType,
      List(metadata),
      NGSReport.Results(
        Some(tumorCellContent.copy(method = Coding(TumorCellContent.Method.Bioinformatic))),
        Some(tmb),
        Some(brcaness),
        Some(hrdScore),
        snvs,
        cnvs,
        //TODO: DNA-/RNA-Fusions, RNASeq
        List.empty,
        List.empty,
        List.empty,
      )
    )


  def genTherapyRecommendation(
    patient: Reference[Patient],
    diagnosis: Reference[MTBDiagnosis],
    variants: Seq[Reference[Variant]]
  ): Gen[MTBMedicationRecommendation] =
    for {
      id <- Gen.of[Id[MTBMedicationRecommendation]]

      priority <- Gen.of[Coding[TherapyRecommendation.Priority.Value]]

      evidenceLevel <-
        for { 
          grading  <- Gen.of[Coding[LevelOfEvidence.Grading.Value]]
          addendum <- Gen.of[Coding[LevelOfEvidence.Addendum.Value]]
          publication <-
            for { 
              pmid <- Gen.uuidStrings
              doi  <- Gen.uuidStrings
            } yield LevelOfEvidence.Publication(
              Some(pmid),
              Some(doi)
            )

        } yield LevelOfEvidence(
          grading,
          Some(Set(addendum)),
          Some(List(publication))
        )

      medication <- Gen.of[Coding[ATC]]

      supportingVariant <- Gen.oneOf(variants)

    } yield MTBMedicationRecommendation(
      id,
      patient,
      diagnosis,
      Some(evidenceLevel),
      priority,
      LocalDate.now,
      Set(medication),
      List(supportingVariant)
    )


  def genCarePlan(
    patient: Reference[Patient],
    diagnosis: Reference[MTBDiagnosis],
    variants: Seq[Reference[Variant]]
  ): Gen[MTBCarePlan] = 
    for { 
      id <- Gen.of[Id[MTBCarePlan]]

      statusReason <- Gen.of[Coding[MTBCarePlan.StatusReason.Value]]

      protocol = "Protocol of the MTB conference..."

      recommendations <- 
        Gen.list(
          Gen.intsBetween(1,3),
          genTherapyRecommendation(
            patient,
            diagnosis,
            variants
          )
        )

      counselingRecommendation <-
        for { 
          crId <- Gen.of[Id[GeneticCounselingRecommendation]]
          reason <- Gen.of[Coding[GeneticCounselingRecommendation.Reason.Value]]
        } yield GeneticCounselingRecommendation(
          crId,
          patient,
          LocalDate.now,
          reason
        )

      studyEnrollmentRecommendation <-
        for { 
          stId <- Gen.of[Id[StudyEnrollmentRecommendation]]
          nctId <- Gen.intsBetween(10000000,50000000)
                     .map(s => ExternalId[Study](s"NCT:$s","NCT"))
        } yield StudyEnrollmentRecommendation(
          stId,
          patient,
          recommendations.head.indication,
          LocalDate.now,
          recommendations.head.levelOfEvidence.map(_.grading),
          recommendations.head.supportingEvidence,
          NonEmptyList.one(nctId)
        )

    } yield MTBCarePlan(
      id,
      patient,
      diagnosis,
      LocalDate.now,
      Some(statusReason),
      Some(protocol),
      recommendations,
      Some(counselingRecommendation),
      List(studyEnrollmentRecommendation)
    )


  def genClaim(
    patient: Reference[Patient],
    recommendation: MTBMedicationRecommendation,
  ): Gen[Claim] =
    for { 
      id <- Gen.of[Id[Claim]]
      status <- Gen.of[Coding[Claim.Status.Value]]
    } yield Claim(
      id,
      patient,
      Reference(recommendation),
      LocalDate.now,
      status
    )

  def genClaimResponse(
    patient: Reference[Patient],
    claim: Reference[Claim]
  ): Gen[ClaimResponse] =
    for { 
      id     <- Gen.of[Id[Claim]]
      status <- Gen.of[Coding[ClaimResponse.Status.Value]]
      statusReason <-
        status match {
          case ClaimResponse.Status(ClaimResponse.Status.Rejected) =>
            Gen.of[Coding[ClaimResponse.StatusReason.Value]].map(Some(_))
          case _ =>
            Gen.const(None)
        }

    } yield ClaimResponse(
      id,
      patient,
      claim,
      LocalDate.now,
      status,
      statusReason
    )


  private implicit val genTherapyStatus: Gen[Coding[Therapy.Status.Value]] =
    Gen.distribution(
      0.1 -> NotDone,
      0.3 -> Ongoing,
      0.4 -> Stopped,
      0.2 -> Completed
    )
    .map(Coding(_))


  def genTherapy(
    patient: Patient,
    diagnosis: Reference[MTBDiagnosis],
    recommendation: MTBMedicationRecommendation
  ): Gen[MTBMedicationTherapy] =
    for {

      id <- Gen.of[Id[MTBMedicationTherapy]]

      status <- Gen.of[Coding[Therapy.Status.Value]]

      statusReason = 
        status match {
          case Therapy.Status(NotDone)   => Some(Coding[Therapy.StatusReason](PaymentRefused))
          case Therapy.Status(Stopped)   => Some(Coding[Therapy.StatusReason](Progression))
          case _                         => None
        }

      period <-
        status match {
          case Therapy.Status(NotDone) => Gen.const(None)
          case _ =>
            val refDate = patient.dateOfDeath.getOrElse(LocalDate.now)
            for {
              duration  <- Gen.longsBetween(8,36)
              start     =  refDate.minusWeeks(duration)
              end       =  refDate
            } yield Some(Period(start,end))
        }

      medication =
        status match {
          case Therapy.Status(NotDone) => None
          case _                       => Some(recommendation.medication)
        }

      note = "Notes on the therapy..."

    } yield MTBMedicationTherapy(
      id,
      Reference(patient),
      diagnosis,
      None,
      Some(Reference(recommendation)),
      Some(LocalDate.now),
      status,
      statusReason,
      period,
      medication,
      Some(note)
    )


  import RECIST._

  private implicit val genRECIST: Gen[Coding[RECIST.Value]] =
    Gen.distribution(
      0.3  -> PD,
      0.3  -> SD,
      0.15 -> PR,
      0.15 -> MR,
      0.1  -> CR
    )
    .map(Coding(_))


  def genResponse(
    patient: Reference[Patient],
    therapy: MTBMedicationTherapy
  ): Gen[Response] =
    for {
      id     <- Gen.of[Id[Response]]
      value  <- Gen.of[Coding[RECIST.Value]]
      followUpTime <- Gen.longsBetween(8,16) 
      date =
        therapy.period
          .map(_.start.plusWeeks(followUpTime))
          .getOrElse(LocalDate.now) 
    } yield Response(
      id,
      patient,
      Reference(therapy),
      date,
      value
    )


  implicit val genPatientRecord: Gen[MTBPatientRecord] =
    for {

      patient <- Gen.of[Patient]

      diagnosis <-
        genDiagnosis(patient)    

      episode <-
         genMTBEpisode(
           Reference(patient),
           List(Reference(diagnosis))
         )

      performanceStatus <-
        genPerformanceStatus(Reference(patient)) 


      guidelineTherapies <-
        Gen.list(
          Gen.intsBetween(1,3),
          genGuidelineTherapy(
            Reference(patient),
            Reference(diagnosis)
          )
        )

      guidelineProcedures <-
        Gen.list(
          Gen.intsBetween(1,3),
          genProcedure(
            Reference(patient),
            Reference(diagnosis)
          )
        )

      specimen <-
        genTumorSpecimen(Reference(patient),diagnosis)

      histologyReport <-
        genHistologyReport(
          Reference(patient),
          Reference(specimen)
        )

      ihcReport <-
        genIHCReport(
          Reference(patient),
          Reference(specimen)
        )

      ngsReport <-
        genNGSReport(
          Reference(patient),
          Reference(specimen)
        )

      carePlan <- 
        genCarePlan(
          Reference(patient),
          Reference(diagnosis),
          ngsReport.variants
            .map(
              v => Reference(v.id,Some(Variant.display(v)))
            )
        )

      claims <-
        Gen.oneOfEach(
          carePlan.medicationRecommendations
            .map(genClaim(Reference(patient),_))
        )

      claimResponses <-
        Gen.oneOfEach(
          claims
            .map(Reference(_))
            .map(genClaimResponse(Reference(patient),_))
        )

      therapies <-
        Gen.oneOfEach(
          carePlan
            .medicationRecommendations
            .map(
              genTherapy(
                patient,
                Reference(diagnosis),
                _
              )
            )
        )

      responses <-
        Gen.oneOfEach(
          therapies
            .map(
              genResponse(
                Reference(patient),
                _
              )
            )
        )


    } yield MTBPatientRecord(
      patient,
      NonEmptyList.one(episode),
      Some(List(diagnosis)),
      Some(guidelineTherapies),
      Some(guidelineProcedures),
      Some(List(performanceStatus)),
      Some(List(specimen)),
      Some(List(histologyReport)),
      Some(List(ihcReport)),
      Some(List(ngsReport)),
      Some(List(carePlan)),
      Some(claims),
      Some(claimResponses),
      Some(therapies.map(List(_)).map(History(_))),
      Some(responses)
    )

}

object Generators extends Generators
