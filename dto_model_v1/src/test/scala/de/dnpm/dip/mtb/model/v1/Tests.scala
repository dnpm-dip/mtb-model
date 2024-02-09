package de.dnpm.dip.mtb.model.v1



import scala.util.Random
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import play.api.libs.json.Json.{
  toJson,
  fromJson,
  prettyPrint
}
import de.ekut.tbi.generators.Gen
import de.bwhc.mtb.dtos.MTBFile
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

    val mtbPatientRecord =
      Gen.of[MTBFile].next
        .pipe(toJson(_))
        .pipe(fromJson[MTBPatientRecord](_))
        .map(_.mapTo[model.MTBPatientRecord])

    mtbPatientRecord.isSuccess must be (true)

  }

}
