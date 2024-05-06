package de.dnpm.dip.mtb.model.v1


import java.net.URI
import scala.util.Random
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Inspectors._
import org.scalatest.matchers.must.Matchers._
import play.api.libs.json.Json.{
  toJson,
  fromJson,
  prettyPrint
}
import de.ekut.tbi.generators.Gen
import de.bwhc.mtb.dtos.{
  MTBFile,
  SomaticNGSReport
}
import de.bwhc.mtb.dto.gens._
import de.dnpm.dip.model.{
  NGSReport,
  Site
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.util.mapping.syntax._
import de.dnpm.dip.mtb.model.v1.mappings._
import de.dnpm.dip.mtb.model



class Tests extends AnyFlatSpec
{

  System.setProperty(Site.property,"UKx:Musterhausen")


  implicit val rnd: Random =
    new Random


  implicit val hgnc: CodeSystem[HGNC] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get
      .latest


  val interpretations =
    Seq(
      "likely_benign",
      "benign",
      "null",
      "likely_oncogenic",
      "oncogenic",
      "uncertain_significance"
    )
    .map(
      Coding(_,URI.create("ClinVar"))
    )


  "Conversion of SNV.interpretation" must "have worked successfully" in {
    interpretations
      .flatMap(_.mapTo[Option[Coding[model.ClinVar.Value]]]) must contain allOf(
        Coding(model.ClinVar.One),
        Coding(model.ClinVar.Two),
        Coding(model.ClinVar.Three),
        Coding(model.ClinVar.Four),
        Coding(model.ClinVar.Five),
      )
  }


  "Parsing v1.MTBPatientRecord from MTBFile JSON and mapping it to model.MTBPatientRecord" must "have suceeded" in {

    // Adapt generator to return only valid sequencing types
    val generator =
      for {
        seqType <- Gen.`enum`(NGSReport.SequencingType)  // DNPM:DIP model SequencingType
        record <- Gen.of[MTBFile]
      } yield record.copy(
        ngsReports =
          record.ngsReports.map(
            _.map(
              ngs => ngs.copy(sequencingType = SomaticNGSReport.SequencingType(seqType.toString)) // bwHC SequencingType
            )
          )
      )


    val mtbPatientRecords =
      LazyList
        .fill(100)(generator.next)
        .map(toJson(_))
        .map(fromJson[MTBPatientRecord](_))
        .tapEach(
          _.fold(errs => errs foreach println, _ => ())
        )
        .map(_.map(_.mapTo[model.MTBPatientRecord]))

    forAll (mtbPatientRecords) { _.isSuccess must be (true) }

  }

}
