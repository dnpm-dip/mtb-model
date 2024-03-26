package de.dnpm.dip.mtb.model.v1



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
  Coding,
  MTBFile,
  Variant
}
import de.bwhc.mtb.dto.gens._
import de.dnpm.dip.coding.CodeSystem
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.util.mapping.syntax._
import de.dnpm.dip.mtb.model.v1.mappings._
import de.dnpm.dip.mtb.model



class Tests extends AnyFlatSpec
{


  implicit val rnd: Random =
    new Random


  implicit val hgnc: CodeSystem[HGNC] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get
      .latest


  "Parsing v1.MTBPatientRecord from MTBFile JSON and mapping it to model.MTBPatientRecord" must "have suceeded" in {

    val mtbPatientRecords =
      LazyList
        .fill(100)(Gen.of[MTBFile].next)
        .map { mtbfile =>
          val ngsReports =
            mtbfile
              .ngsReports.getOrElse(List.empty)
              .map(
                ngs =>
                  ngs.copy(
                    simpleVariants =
                      ngs.simpleVariants.map(
                        _.map(sv =>
                          sv.copy(interpretation = Coding(Variant.Interpretation("0"),None,None))
                        )
                      )
                  )
              )

          mtbfile.copy(
            ngsReports = Some(ngsReports)
          )
        }
        .map(toJson(_))
        .map(fromJson[MTBPatientRecord](_))
        .tapEach(
          _.fold(errs => errs foreach println, _ => ())
        )
        .map(_.map(_.mapTo[model.MTBPatientRecord]))

    forAll (mtbPatientRecords) { _.isSuccess must be (true) }

  }

}
