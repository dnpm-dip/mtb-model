package de.dnpm.dip.mtb.gens


import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.mtb.model.MTBPatientRecord
import play.api.libs.json.Json.{
  toJson,
  prettyPrint
}
import play.api.libs.json.Writes


class Tests extends AnyFlatSpec
with Generators
{

  implicit val rnd: Random =
    new Random


  private def printJson[T: Writes](t: T) =
    println(prettyPrint(toJson(t)))

 
  "Generation of MTBPatientRecord" must "have worked" in { 

    val record =
      Gen.of[MTBPatientRecord].next

    printJson(record)

  }


}