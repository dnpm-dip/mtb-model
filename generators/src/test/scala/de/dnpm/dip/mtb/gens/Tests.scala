package de.dnpm.dip.mtb.gens


import scala.util.Random
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.mtb.model.MTBPatientRecord


class Tests extends AnyFlatSpec
with Generators
{

  implicit val rnd: Random =
    new Random

 
  "Generation of MTBPatientRecord" must "have worked" in { 

    val record =
      Gen.of[MTBPatientRecord].next


    record.getNgsReports.flatMap(_.variants) must not be empty

    record.getCarePlans
      .flatMap(_.medicationRecommendations.getOrElse(List.empty))
      .flatMap(_.supportingVariants) must not be empty

  }


}
