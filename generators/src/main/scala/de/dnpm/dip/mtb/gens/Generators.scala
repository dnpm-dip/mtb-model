package de.dnpm.dip.mtb.gens



import java.net.URI
import java.time.LocalDate
import java.time.temporal.ChronoUnit.{
  MONTHS,
  YEARS
}
import cats.data.NonEmptyList
import shapeless.Coproduct
import shapeless.ops.coproduct.Selector
import de.ekut.tbi.generators.Gen
import de.ekut.tbi.generators.DateTimeGens._
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem
}
import de.dnpm.dip.coding.hgnc.HGNC
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
import de.dnpm.dip.util.DisplayLabel
import de.dnpm.dip.model.{
  Address,
  BaseVariant,
  ClosedInterval,
  ExternalId,
  FollowUp,
  Gender,
  GeneAlterationReference,
  HealthInsurance,
  IK,
  Id,
  History,
  Medications,
  NGSReport,
  Patient,
  Period,
  Publication,
  PubMed,
  Reference,
  Recommendation,
  Study,
  Therapy
}
import de.dnpm.dip.mtb.model._
import MTBTherapy.StatusReason.{
  PaymentRefused,
  Progression
}
import TumorStaging.TNM.UICC


trait Generators
{


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
      "AACS",
      "AAGAB",
      "ABCA3",
      "ABCA4",
      "ABRAXAS1",
      "ACAD10",
      "ABL1",
      "AKT1",
      "ATRX",
      "BRCA1",
      "BRAF",
      "BRAFP1",
      "CDH13",
      "CDK6",
      "EGFR",
      "FGFR2",
      "FGFR3",
      "HRAS",
      "KRAS",
      "MDM2",
      "MTOR",
      "RET",
      "TP53",
    )


  private implicit lazy val hgnc: CodeSystem[HGNC] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get
      .latest
      .filter(gene => symbols contains gene.display)


  implicit def genEnum[E <: Enumeration](
    implicit w: shapeless.Witness.Aux[E]
  ): Gen[E#Value] =
    Gen.`enum`(w.value)


  implicit def genId[T]: Gen[Id[T]] =
    Gen.uuidStrings
      .map(Id(_))

  implicit def genReference[T]: Gen[Reference[T]] =
    Gen.of[Id[T]]
      .map(Reference(_))


  implicit def genExternalId[T,S: Coding.System]: Gen[ExternalId[T,S]] =
    Gen.uuidStrings
      .map(ExternalId[T,S](_))

  implicit def genExternalIdSysUnion[T,S <: Coproduct](
    implicit uris: Coding.System.UriSet[S]
  ): Gen[ExternalId[T,S]] =
    for {
      sys <- Gen.oneOf(uris.values.toSeq)
      id  <- Gen.uuidStrings
    } yield ExternalId[T,S](id,sys)


  implicit def genCodingfromCodeSystem[S: Coding.System: CodeSystem]: Gen[Coding[S]] =
    Gen.oneOf(CodeSystem[S].concepts)
      .map(_.toCoding)


  private val genGender: Gen[Coding[Gender.Value]] =
    Gen.distribution(
      49.0 -> Gender.Male,
      49.0 -> Gender.Female,
      2.0  -> Gender.Other,
    )
    .map(Coding(_))


  implicit val genPatient: Gen[Patient] =
    for {
      id <- Gen.of[Id[Patient]]

      gender <- genGender

      birthDate <-
        localDatesBetween(
          LocalDate.now.minusYears(70),
          LocalDate.now.minusYears(30)
        )

      age = YEARS.between(birthDate,LocalDate.now)

      dateOfDeath <-
        Gen.option(
          Gen.longsBetween(age - 20L, age - 5L)
            .map(birthDate.plusYears),
          0.4
        )

      healthInsurance =
        Patient.Insurance(
          Coding(HealthInsurance.Type.GKV),
          Some(
            Reference(ExternalId[HealthInsurance,IK]("1234567890"))
              .withDisplay("AOK")
            )
        )

    } yield Patient(
      id,
      gender,
      birthDate,
      dateOfDeath,
      None,
      healthInsurance,
      Address("12345")
    )


  def genDiagnosis(
    patient: Patient
  ): Gen[MTBDiagnosis] =
    for {
      id <- Gen.of[Id[MTBDiagnosis]]

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

      icd10 <- Gen.of[Coding[ICD10GM]]

      icdo3 =
        icdo3Topography
          .concepts
          .find(_.code.value == icd10.code.value)
          .map(_.toCoding)
          .getOrElse(
            Coding[ICDO3.Topography](
              "T",
              "Topographie",
              icdo3Topography.version.get
            )
          )

      typ =
        MTBDiagnosis.Type(
          Coding(MTBDiagnosis.Type.Main),
          date
        )

      grading <-
        for {
          obds <- Gen.of[Coding[OBDSGrading.Value]].map(Coding[TumorGrading.Systems].from(_))
          who <- Gen.of[Coding[WHOGrading]].map(Coding[TumorGrading.Systems].from(_))
        } yield TumorGrading(
          date,
          NonEmptyList.of(
            obds,
            who
          )
        )

      staging <- for {

        tnm <- for {
          t <- Gen.oneOf("Tx","T0","T1","T2","T3","T4").map(Coding[UICC](_))
          n <- Gen.oneOf("Nx","N0","N1","N2","N3").map(Coding[UICC](_))
          m <- Gen.oneOf("Mx","M0","M1").map(Coding[UICC](_))
        } yield TumorStaging.TNM(t,n,m)

        spread <- Gen.of[Coding[TumorStaging.KDSSpread.Value]]
                    .map(Coding[TumorStaging.OtherSystems].from(_))

      } yield TumorStaging(
        date,
        Coding(TumorStaging.Method.Clinical),
        Some(tnm),
        Some(List(spread))
      )

      glts <- Gen.of[Coding[MTBDiagnosis.GuidelineTreatmentStatus.Value]]

    } yield MTBDiagnosis(
      id,
      Reference.to(patient),
      date,
      History(typ),
      icd10,
      None,
      icdo3,
      Some(History(grading)),
      Some(History(staging)),
      Some(glts),
      None,
      Some(List("Notes on the tumor diagnosis..."))
    )


  def genEpisode(
    patient: Reference[Patient],
    diagnoses: List[Reference[MTBDiagnosis]]
  ): Gen[MTBEpisodeOfCare] =
    for {
      id <- Gen.of[Id[MTBEpisodeOfCare]]

      period = Period(LocalDate.now.minusMonths(6))
     
    } yield MTBEpisodeOfCare(
      id,
      patient,
      period,
      Some(diagnoses)
    )


  def genPerformanceStatus(
    patient: Reference[Patient]
  ): Gen[PerformanceStatus] =
    for {
      id <- Gen.of[Id[PerformanceStatus]]
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
    diagnosis: MTBDiagnosis,
  ): Gen[MTBSystemicTherapy] =
    for {
      id <-
        Gen.of[Id[MTBSystemicTherapy]]

      therapyLine <-
        Gen.intsBetween(1,9)

      intent <- Gen.of[Coding[MTBTherapy.Intent.Value]]

      category <- Gen.of[Coding[MTBSystemicTherapy.Category.Value]]

      status =
        Coding(Stopped)

      statusReason =
        Coding(Progression)

      period <- 
        for {
          monthsAgo <- Gen.longsBetween(12,24)
          duration  <- Gen.longsBetween(8,36)
          start     =  LocalDate.now.minusMonths(monthsAgo)
          end       =  start.plusWeeks(duration)
        } yield Period(start,end)

      medication <-
        Gen.of[Coding[ATC]]
          .map(_.asInstanceOf[Coding[Medications]])
          .map(Set(_))

      note = "Notes on the therapy..."

    } yield MTBSystemicTherapy(
      id,
      patient,
      Some(
        Reference.to(
          diagnosis,
          Some(DisplayLabel.of(diagnosis.code).value)
        )
      ),
      Some(therapyLine),
      Some(intent),
      Some(category),
      None,
      LocalDate.now,
      status,
      Some(statusReason),
      None,
      None,
      Some(period),
      Some(medication),
      Some(List(note))
    )


  def genProcedure(
    patient: Reference[Patient],
    diagnosis: MTBDiagnosis,
  ): Gen[OncoProcedure] =
    for { 
      id <- Gen.of[Id[OncoProcedure]]

      code <- Gen.of[Coding[OncoProcedure.Type.Value]]

      intent <- Gen.of[Coding[MTBTherapy.Intent.Value]]

      status <- Gen.of[Coding[Therapy.Status.Value]]

      statusReason <- Gen.of[Coding[MTBTherapy.StatusReason.Value]]

      therapyLine <- Gen.intsBetween(1,9)

      period = Period(LocalDate.now.minusMonths(6))

      note = "Notes on the therapeutic procedure..."
    } yield OncoProcedure(
      id,
      patient,
      Some(
        Reference.to(
          diagnosis,
          Some(DisplayLabel.of(diagnosis.code).value)
        )
      ),
      Some(therapyLine),
      Some(intent),
      None,
      code,
      status,
      Some(statusReason),
      LocalDate.now,
      Some(period),
      Some(List(note))
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
      Reference.to(diag),
      typ,
      Some(
        TumorSpecimen.Collection(
          Some(LocalDate.now),
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


  def genMolecularDiagnosticReport(
    patient: Reference[Patient],
    specimen: TumorSpecimen
  ): Gen[MolecularDiagnosticReport] =
    for { 
      id <- Gen.of[Id[MolecularDiagnosticReport]]
      typ <- Gen.of[Coding[MolecularDiagnosticReport.Type.Value]]
    } yield MolecularDiagnosticReport(
      id,
      patient,
      Some(
        Reference(Id[Institute]("xyz")).withDisplay("Molekular-Pathologie UKx")
      ),
      LocalDate.now,
      Reference.to(specimen),
      typ,
      Some(List("Result of diagnostics..."))
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
        morphology,
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

      journalId <- Gen.of[Id[Nothing]]

      blockId <- Gen.of[Id[Nothing]]

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
      NonEmptyList.of(blockId),
      IHCReport.Results(
        proteinExpression,
        List.empty,
      )
    )



  private val bases = 
    Seq("A","C","G","T")


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


  protected def genVariantId[
    T <: Variant,
    S: Coding.System
  ](
    implicit sel: Selector[Variant.Systems,S]
  ): Gen[ExternalId[T,Variant.Systems]] =
    Gen.uuidStrings
      .map(ExternalId[T,S](_))
      .map(_.asInstanceOf[ExternalId[T,Variant.Systems]])



  def genSNV(patient: Reference[Patient]): Gen[SNV] =
    for { 
      id <-
        Gen.of[Id[Variant]]

      dbSnpId <- genVariantId[SNV,dbSNP]

      cosmicId <- genVariantId[SNV,COSMIC]
      
      chr <- Gen.of[Chromosome.Value]

      gene <- Gen.of[Coding[HGNC]]

      localization <- Gen.of[Coding[BaseVariant.Localization.Value]]

      transcriptId <- Gen.of[ExternalId[Transcript,Transcript.Systems]]

      exonId <- Gen.intsBetween(2,12).map(exon => Id[Exon](exon.toString))

      position <- Gen.longsBetween(24,600)

      ref <- Gen.oneOf(bases)    

      alt <- Gen.oneOf(bases filter (_ != ref))

      dnaChg = Code[HGVS.DNA](s"c.$position$ref>$alt")

      proteinChg <- Gen.oneOf(proteinChanges).map(Code[HGVS.Protein](_))

      readDepth <- Gen.intsBetween(5,25).map(SNV.ReadDepth(_))

      allelicFreq <- Gen.doubles.map(SNV.AllelicFrequency(_))

      interpretation <- Gen.of[Coding[ClinVar.Value]]

    } yield SNV(
      id,
      patient,
      Some(List(dbSnpId,cosmicId)),
      chr,
      gene,
      Some(Set(localization)),
      transcriptId,
      Some(exonId),
      Variant.PositionRange(position,None),
      SNV.Allele(alt),
      SNV.Allele(ref),
      dnaChg,
      Some(proteinChg),
      readDepth,
      allelicFreq,
      Some(interpretation)
    )


  def genCNV(patient: Reference[Patient]): Gen[CNV] =
    for { 
      id <- Gen.of[Id[Variant]]
      
      chr <- Gen.of[Chromosome.Value]

      localization <- Gen.of[Coding[BaseVariant.Localization.Value]]

      startRange <- Gen.longsBetween(42L,50000L).map(start => Variant.PositionRange(start,Some(start+42L)))

      length <- Gen.longsBetween(42L,1000L)

      endRange = Variant.PositionRange(startRange.start + length,Some(startRange.start + length + 50L))

      copyNum <- Gen.intsBetween(1,8)

      relCopyNum <- Gen.doubles

      cnA <- Gen.doubles

      cnB <- Gen.doubles

      affectedGenes <- Gen.list(Gen.intsBetween(4,20),Gen.of[Coding[HGNC]])

      focality = "partial q-arm"

      copyNumberNeutralLoH <- Gen.list(Gen.intsBetween(3,15),Gen.of[Coding[HGNC]])

      typ = copyNum match { 
        case n if n < 2 => CNV.Type.Loss
        case n if n < 4 => CNV.Type.LowLevelGain
        case _          => CNV.Type.HighLevelGain
      }

    } yield CNV(
      id,
      patient,
      None,
      chr,
      Some(Set(localization)),
      Some(startRange),
      Some(endRange),
      Some(copyNum),
      Some(relCopyNum),
      Some(cnA),
      Some(cnB),
      Some(affectedGenes.distinctBy(_.code).toSet),
      Some(focality),
      Coding(typ),
      Some(copyNumberNeutralLoH.distinctBy(_.code).toSet)
    )

  implicit val genDNAFusionPartner: Gen[DNAFusion.Partner] =
    for { 
      chr <- Gen.of[Chromosome.Value]
      gene <- Gen.of[Coding[HGNC]]
      position <- Gen.longsBetween(42L,1000L)
    } yield DNAFusion.Partner(
      chr,
      gene,
      position
    )

  def genDNAFusion(patient: Reference[Patient]): Gen[DNAFusion] =
    for {
      id <- Gen.of[Id[Variant]]
      localization <- Gen.of[Coding[BaseVariant.Localization.Value]]
      partner5pr <- Gen.of[DNAFusion.Partner]
      partner3pr <- Gen.of[DNAFusion.Partner]
      reads <- Gen.intsBetween(3,10)
    } yield DNAFusion(
      id,
      patient,
      None,
      Some(Set(localization)),
      partner5pr,
      partner3pr,
      reads
    )


  implicit val genRNAFusionPartner: Gen[RNAFusion.Partner] =
    for { 
      trancriptId <- Gen.of[ExternalId[Transcript,Transcript.Systems]]
      gene <- Gen.of[Coding[HGNC]]
      exonId <- Gen.intsBetween(2,12).map(exon => Id[Exon](exon.toString))
      position <- Gen.longsBetween(42L,1000L)
      strand <- Gen.of[RNAFusion.Strand.Value]
    } yield RNAFusion.Partner(
      trancriptId,
      exonId,
      gene,
      position,
      strand
    )

  def genRNAFusion(patient: Reference[Patient]): Gen[RNAFusion] =
    for {
      id <- Gen.of[Id[Variant]]
      localization <- Gen.of[Coding[BaseVariant.Localization.Value]]
      partner5pr <- Gen.of[RNAFusion.Partner]
      partner3pr <- Gen.of[RNAFusion.Partner]
      cosmicId <- genVariantId[RNAFusion,COSMIC]
      reads <- Gen.intsBetween(3,10)
    } yield RNAFusion(
      id,
      patient,
      Some(List(cosmicId)),
      Some(Set(localization)),
      partner5pr,
      partner3pr,
      Some("Effect"),
      reads
    )


  def genNGSReport(
    patient: Reference[Patient],
    specimen: Reference[TumorSpecimen]
  ): Gen[SomaticNGSReport] =
    for {
      id <- Gen.of[Id[SomaticNGSReport]]

      seqType <-
        Gen.of[Coding[NGSReport.Type.Value]]

      metadata <-
        for {
          refGenome <- Gen.oneOf("HG19","HG38","GRCh37")
        } yield SomaticNGSReport.Metadata(
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
          TMB.Result(value.toDouble),
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

      dnaFusions <-
        Gen.list(
          Gen.intsBetween(4,10),
          genDNAFusion(patient)
        )  

      rnaFusions <-
        Gen.list(
          Gen.intsBetween(4,10),
          genRNAFusion(patient)
        )  

    } yield SomaticNGSReport(
      id,
      patient,
      specimen,
      LocalDate.now,
      seqType,
      NonEmptyList.of(metadata),
      SomaticNGSReport.Results(
        Some(tumorCellContent.copy(method = Coding(TumorCellContent.Method.Bioinformatic))),
        Some(tmb),
        Some(brcaness),
        Some(hrdScore),
        Some(snvs),
        Some(cnvs),
        Some(dnaFusions),
        Some(rnaFusions),
        Some(List.empty)  //TODO: RNASeq
      )
    )


  def genSystemicTherapyRecommendation(
    patient: Reference[Patient],
    diagnosis: MTBDiagnosis,
    variants: Seq[Variant]
  ): Gen[MTBMedicationRecommendation] =
    for {
      id <- Gen.of[Id[MTBMedicationRecommendation]]

      priority <- Gen.of[Coding[Recommendation.Priority.Value]]

      evidenceLevel <-
        for { 
          grading  <- Gen.of[Coding[LevelOfEvidence.Grading.Value]]
          addendum <- Gen.of[Coding[LevelOfEvidence.Addendum.Value]]
          publication <-
            Gen.positiveInts
              .map(_.toString)
              .map(ExternalId[Publication,PubMed](_))
              .map(Reference(_))

        } yield LevelOfEvidence(
          grading,
          Some(Set(addendum)),
          Some(List(publication))
        )

      medication <- Gen.of[Coding[ATC]]

      category <- Gen.of[Coding[MTBMedicationRecommendation.Category.Value]]

      useType <- Gen.of[Coding[MTBMedicationRecommendation.UseType.Value]]

      supportingVariant <- Gen.oneOf(variants).map {
        variant =>
          GeneAlterationReference(
            Reference.to(variant),
            variant match {
              case snv: SNV          => Some(snv.gene)
              case cnv: CNV          => cnv.reportedAffectedGenes.flatMap(_.headOption)
              case fusion: DNAFusion => Some(fusion.fusionPartner5prime.gene)
              case fusion: RNAFusion => Some(fusion.fusionPartner5prime.gene)
              case rnaSeq: RNASeq    => rnaSeq.gene
            }
          )
      }

    } yield MTBMedicationRecommendation(
      id,
      patient,
      Some(Reference.to(diagnosis,Some(DisplayLabel.of(diagnosis.code).value))),
      LocalDate.now,
      priority,
      Some(evidenceLevel),
      Some(category),
      Set(medication),
      Some(useType),
      Some(List(supportingVariant))
    )

  def genOtherTherapyRecommendation(
    patient: Reference[Patient],
    diagnosis: MTBDiagnosis,
    variants: Seq[Variant]
  ): Gen[MTBProcedureRecommendation] =
    for {
      id <- Gen.of[Id[MTBProcedureRecommendation]]

      priority <- Gen.of[Coding[Recommendation.Priority.Value]]

      evidenceLevel <-
        for { 
          grading  <- Gen.of[Coding[LevelOfEvidence.Grading.Value]]
          addendum <- Gen.of[Coding[LevelOfEvidence.Addendum.Value]]
          publication <-
            Gen.positiveInts
              .map(_.toString)
              .map(ExternalId[Publication,PubMed](_))
              .map(Reference(_))

        } yield LevelOfEvidence(
          grading,
          Some(Set(addendum)),
          Some(List(publication))
        )

      category <- Gen.of[Coding[MTBProcedureRecommendation.Category.Value]]

      supportingVariant <- Gen.oneOf(variants).map {
        variant =>
          GeneAlterationReference(
            Reference.to(variant),
            variant match {
              case snv: SNV          => Some(snv.gene)
              case cnv: CNV          => cnv.reportedAffectedGenes.flatMap(_.headOption)
              case fusion: DNAFusion => Some(fusion.fusionPartner5prime.gene)
              case fusion: RNAFusion => Some(fusion.fusionPartner5prime.gene)
              case rnaSeq: RNASeq    => rnaSeq.gene
            }
          )
      }

    } yield MTBProcedureRecommendation(
      id,
      patient,
      Some(Reference.to(diagnosis,Some(DisplayLabel.of(diagnosis.code).value))),
      LocalDate.now,
      priority,
      Some(evidenceLevel),
      category,
      Some(List(supportingVariant))
    )


  def genCarePlan(
    patient: Reference[Patient],
    diagnosis: MTBDiagnosis,
    specimen: TumorSpecimen,
    variants: Seq[Variant]
  ): Gen[MTBCarePlan] = 
    for { 
      id <- Gen.of[Id[MTBCarePlan]]

//      statusReason <- Gen.of[Coding[MTBCarePlan.StatusReason.Value]]

      protocol = "Protocol of the MTB conference..."

      medicationRecommendations <- 
        Gen.list(
          Gen.intsBetween(1,3),
          genSystemicTherapyRecommendation(
            patient,
            diagnosis,
            variants
          )
        )

      procedureRecommendation <- 
        genOtherTherapyRecommendation(
          patient,
          diagnosis,
          variants
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
          recId  <- Gen.of[Id[MTBStudyEnrollmentRecommendation]]
          priority <- Gen.of[Coding[Recommendation.Priority.Value]]
          studyRef <- Gen.of[ExternalId[Study,Study.Registries]].map(Reference(_))
        } yield MTBStudyEnrollmentRecommendation(
          recId,
          patient,
          medicationRecommendations.head.reason.get,
          LocalDate.now,
          medicationRecommendations.head.levelOfEvidence,
          priority,
          NonEmptyList.of(studyRef),
          None,
          medicationRecommendations.head.supportingVariants,
        )

      rebiopyRequest <-
        for { 
          id <- Gen.of[Id[RebiopsyRequest]]
        } yield RebiopsyRequest(
          id,
          patient,
          Reference.to(diagnosis),
          LocalDate.now,
        )

      reevaluationRequest <-
        for { 
          id <- Gen.of[Id[HistologyReevaluationRequest]]
        } yield HistologyReevaluationRequest(
          id,
          patient,
          Reference.to(specimen),
          LocalDate.now
        )

    } yield MTBCarePlan(
      id,
      patient,
      Some(Reference.to(diagnosis,Some(DisplayLabel.of(diagnosis.code).value))),
      LocalDate.now,
//      Some(statusReason),
      None,
      None,
      Some(counselingRecommendation),
      Some(medicationRecommendations),
      Some(List(procedureRecommendation)),
      Some(List(studyEnrollmentRecommendation)),
      Some(List(reevaluationRequest)),
      Some(List(rebiopyRequest)),
      Some(List(protocol))
    )


  def genClaim(
    patient: Reference[Patient],
    recommendation: MTBMedicationRecommendation,
  ): Gen[Claim] =
    for { 
      id <- Gen.of[Id[Claim]]
      stage <- Gen.of[Coding[Claim.Stage.Value]]
    } yield Claim(
      id,
      patient,
      Reference.to(recommendation),
      None,
      LocalDate.now,
      Some(stage)
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
    diagnosis: MTBDiagnosis,
    recommendation: MTBMedicationRecommendation
  ): Gen[MTBSystemicTherapy] =
    for {

      id <- Gen.of[Id[MTBSystemicTherapy]]

      intent <- Gen.of[Coding[MTBTherapy.Intent.Value]]

      category <- Gen.of[Coding[MTBSystemicTherapy.Category.Value]]

      status <- Gen.of[Coding[Therapy.Status.Value]]

      statusReason = 
        status match {
          case Therapy.Status(NotDone) => Some(Coding(PaymentRefused))
          case Therapy.Status(Stopped) => Some(Coding(Progression))
          case _                       => None
        }

      period <-
        status match {
          case Therapy.Status(NotDone) => Gen.const(None)
          case _ =>
            val refDate = patient.dateOfDeath.getOrElse(LocalDate.now)
            for {
              duration <- Gen.longsBetween(8,36)
              start    =  refDate.minusWeeks(duration)
              end      =  refDate
            } yield Some(Period(start,end))
        }

      medication =
        status match {
          case Therapy.Status(NotDone) => None
          case _                       => Some(recommendation.medication)
        }

      fulfillmentStatus <- Gen.of[Coding[MTBSystemicTherapy.RecommendationFulfillmentStatus.Value]]  

      dosage <- Gen.of[Coding[MTBSystemicTherapy.DosageDensity.Value]]

      note = "Notes on the therapy..."

    } yield MTBSystemicTherapy(
      id,
      Reference.to(patient),
      Some(Reference.to(diagnosis,Some(DisplayLabel.of(diagnosis.code).value))),
      None,
      Some(intent),
      Some(category),
      Some(Reference.to(recommendation)),
      LocalDate.now,
      status,
      statusReason,
      Some(fulfillmentStatus),
      Some(dosage),
      period,
      medication,
      Some(List(note))
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
    therapy: MTBSystemicTherapy
  ): Gen[Response] =
    for {
      id     <- Gen.of[Id[Response]]
      value  <- Gen.of[Coding[RECIST.Value]]
      method <- Gen.of[Coding[Response.Method.Value]]
      followUpTime <- Gen.longsBetween(8,16) 
      date =
        therapy.period
          .map(_.start.plusWeeks(followUpTime))
          .getOrElse(LocalDate.now) 
    } yield Response(
      id,
      patient,
      Reference.to(therapy),
      date,
      method,
      value
    )


  implicit val genPatientRecord: Gen[MTBPatientRecord] =
    for {

      patient <- Gen.of[Patient]

      patRef = Reference.to(patient)

      diagnosis <-
        genDiagnosis(patient)    

      episode <-
         genEpisode(
           patRef,
           List(Reference.to(diagnosis))
         )

      performanceStatus <-
        genPerformanceStatus(patRef) 


      guidelineTherapies <-
        Gen.list(
          Gen.intsBetween(1,3),
          genGuidelineTherapy(
            patRef,
            diagnosis
          )
        )

      guidelineProcedures <-
        Gen.list(
          Gen.intsBetween(1,3),
          genProcedure(
            patRef,
            diagnosis
          )
        )

      specimen <-
        genTumorSpecimen(patRef,diagnosis)

      priorDiagnostics <- 
        genMolecularDiagnosticReport(patRef,specimen)
        
      histologyReport <-
        genHistologyReport(
          patRef,
          Reference.to(specimen)
        )

      ihcReport <-
        genIHCReport(
          patRef,
          Reference.to(specimen)
        )

      ngsReport <-
        genNGSReport(
          patRef,
          Reference.to(specimen)
        )

      carePlan <- 
        genCarePlan(
          patRef,
          diagnosis,
          specimen,
          ngsReport.variants
        )

      recommendations =
        carePlan.medicationRecommendations
          .getOrElse(List.empty)

      claims <-
        Gen.oneOfEach(
          recommendations
            .map(genClaim(patRef,_))
        )

      claimResponses <-
        Gen.oneOfEach(
          claims
            .map(Reference.to(_))
            .map(genClaimResponse(patRef,_))
        )

      therapies <-
        Gen.oneOfEach(
          recommendations
            .map(
              genTherapy(
                patient,
                diagnosis,
                _
              )
            )
        )

      responses <-
        Gen.oneOfEach(
          therapies
            .map(
              genResponse(patRef,_)
            )
        )

      followUp = 
        FollowUp(
          responses.head.effectiveDate,
          patRef,
          Some(responses.head.effectiveDate),
          None
        )
        

    } yield MTBPatientRecord(
      patient,
      NonEmptyList.one(episode),
      NonEmptyList.one(diagnosis),
      Some(guidelineTherapies),
      Some(guidelineProcedures),
      Some(List(performanceStatus)),
      Some(List(specimen)),
      Some(List(priorDiagnostics)),
      Some(List(histologyReport)),
      Some(List(ihcReport)),
      Some(List(ngsReport)),
      Some(List(carePlan)),
      Some(List(followUp)),
      Some(claims),
      Some(claimResponses),
      Some(therapies.map(History(_))),
      Some(responses)
    )

}

object Generators extends Generators
