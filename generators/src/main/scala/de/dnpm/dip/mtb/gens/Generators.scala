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
import de.dnpm.dip.coding.hgnc.{
  HGNC,
  Ensembl
}
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.atc.Kinds.Substance
import de.dnpm.dip.coding.icd.{
  ICD10GM,
  ICD10GMCatalogs,
  ICDO3,
  ClassKinds
}
import ClassKinds.Category
import de.dnpm.dip.coding.icd.ICDO3.extensions._
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
  Therapy
}
import de.dnpm.dip.mtb.model._



trait Generators
{

  import MTBMedicationTherapy.statusReasonCodeSystem


  private implicit lazy val icd10gm: CodeSystem[ICD10GM] =
    ICD10GMCatalogs
      .getInstance[cats.Id]
      .get
      .latest
      .filter(_.code.value startsWith "C")


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


  private implicit lazy val atc: CodeSystem[ATC] =
    ATC.Catalogs
      .getInstance[cats.Id]
      .get
      .latest
      .filter(_.code.value startsWith "L01XX")
      .filter(ATC.filterByKind(Substance))


  private implicit lazy val hgnc: CodeSystem[HGNC] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get
      .latest



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


  import Therapy.Status._

  val genGuidelineTherapy: Gen[MTBMedicationTherapy] =
    for {
      id <- Gen.of[Id[MTBMedicationTherapy]]

      patient <- Gen.of[Reference[Patient]]

      indication <- Gen.of[Reference[MTBDiagnosis]]

      therapyLine <- Gen.intsBetween(1,9)

      status <- Gen.of[Coding[Therapy.Status.Value]]

      statusReason <- Gen.of[Coding[Therapy.StatusReason]]

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


  implicit val genOncoProcedure: Gen[OncoProcedure] =
    for { 
      id <- Gen.of[Id[OncoProcedure]]

      patient <- Gen.of[Reference[Patient]]

      indication <- Gen.of[Reference[MTBDiagnosis]]

      code <- Gen.of[Coding[OncoProcedure.Type.Value]]

      status <- Gen.of[Coding[Therapy.Status.Value]]

      statusReason <- Gen.of[Coding[Therapy.StatusReason]]

      therapyLine <- Gen.intsBetween(1,9)

      period = Period(LocalDate.now.minusMonths(6))

      note = "Notes on the therapy..."
    } yield OncoProcedure(
      id,
      patient,
      indication,
      code,
      status,
      Some(statusReason),
      Some(therapyLine),
      None,
      Some(LocalDate.now),
      Some(period),
      Some(note)
    )


  implicit val genTumorSpecimen: Gen[TumorSpecimen] =
    for {
      id <- Gen.of[Id[TumorSpecimen]]

      patient <- Gen.of[Reference[Patient]]

      typ <- Gen.of[Coding[TumorSpecimen.Type.Value]]

      method <- Gen.of[Coding[TumorSpecimen.Collection.Method.Value]]

      localization <- Gen.of[Coding[TumorSpecimen.Collection.Localization.Value]]

    } yield TumorSpecimen(
      id,
      patient,
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

  private val aminoAcids = 
    Seq(
      "Ala", "Asx", "Cys", "Asp",
      "Glu", "Phe", "Gly", "His",
      "Ile", "Lys", "Leu", "Met",
      "Asn", "Pro", "Gln", "Arg",
      "Ser", "Thr", "Sec", "Val",
      "Trp", "Tyr", "Glx"
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
          .map(ExternalId[Transcript](_,Some(Coding.System[Ensembl].uri)))

      position <-
        Gen.longsBetween(24,600)

      ref <- Gen.oneOf(bases)    

      alt <- Gen.oneOf(bases filter (_ != ref))

      dnaChg = Coding[HGVS](s"c.$position$ref>$alt")

      refAA <- Gen.oneOf(aminoAcids)

      altAA <- Gen.oneOf(aminoAcids filter (_ != refAA))

      proteinChg = Coding[HGVS](s"p.$refAA${(position/3).toInt}$altAA")

      readDepth <- Gen.intsBetween(2,25).map(SNV.ReadDepth(_))

      allelicFreq <- Gen.doubles.map(SNV.AllelicFrequency(_))

      interpretation <- Gen.of[Coding[ClinVar]]

    } yield SNV(
      id,
      patient,
      Set(dbSnpId,cosmicId),
      chr,
      Some(gene),
      transcriptId,
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
      affectedGenes,
      Some(focality),
      Coding(typ),
      copyNumberNeutralLoH
    )


  def genNGSReport(
    patient: Reference[Patient],
    specimen: Reference[TumorSpecimen]
  ): Gen[NGSReport] =
    for {
      id <- Gen.of[Id[NGSReport]]

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
      List(metadata),
      NGSReport.Results(
        Some(tumorCellContent.copy(method = Coding(TumorCellContent.Method.Bioinformatic))),
        Some(brcaness),
        Some(hrdScore),
        snvs,
        cnvs,
        List.empty,
        List.empty,
        List.empty,
      )
    )



  implicit val genResponse: Gen[Response] =
    for {
      id <- Gen.of[Id[Response]]

      patient <- Gen.of[Reference[Patient]]

      therapy <- Gen.of[Reference[MTBMedicationTherapy]]

      value <- Gen.of[Coding[RECIST.Value]]

    } yield Response(
      id,
      patient,
      therapy,
      LocalDate.now,
      value
    )



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
        .map(
          _.map(
            _.copy(
              patient = patRef,
              indication = Reference(diagnosis)
            )
          )
        )

      guidelineProcedures <-
        Gen.list(
          Gen.intsBetween(1,3),
          Gen.of[OncoProcedure]
        )
        .map(
          _.map(
            _.copy(
              patient = patRef,
              indication = Reference(diagnosis)
            )
          )
        )

      specimen <-
        Gen.of[TumorSpecimen]
          .map(
            _.copy(patient = patRef)
          )

      histologyReport <-
        genHistologyReport(
          Reference(patient),
          Reference(specimen)
        )

      ngsReport <-
        genNGSReport(
          Reference(patient),
          Reference(specimen)
        )

    } yield MTBPatientRecord(
      patient,
      NonEmptyList.one(episode),
      NonEmptyList.one(diagnosis),
      Some(guidelineTherapies),
      Some(guidelineProcedures),
      Some(List(performanceStatus)),
      Some(List(specimen)),
      Some(List(histologyReport)),
      Some(List(ngsReport)),
      None,
      None,
      None,
    )

}
