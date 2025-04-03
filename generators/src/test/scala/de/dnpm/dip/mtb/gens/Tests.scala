package de.dnpm.dip.mtb.gens


import scala.util.Random
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.mtb.model.MTBPatientRecord
import de.dnpm.dip.mtb.model.json.Schemas
import play.api.libs.json.Json.{
  toJson,
  prettyPrint,
  stringify
}
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.{
  JsonSchemaFactory,
  SpecVersion
}
import scala.jdk.CollectionConverters._
import json.Schema
import json.schema.Version._
import com.github.andyglow.jsonschema.AsPlay._



class Tests extends AnyFlatSpec
with Generators
with Schemas
{

  implicit val rnd: Random =
    new Random

 
  "MTBPatientRecord" must "have have been successfully generated" in { 

    val record =
      Gen.of[MTBPatientRecord].next
        .tap {
           record =>
            import java.io.FileWriter
            import scala.util.Using

            Using(new FileWriter("/home/lucien/mtb_patient_record.json")){
              _.write(prettyPrint(toJson(record)))
            }
        }


    record.getNgsReports.flatMap(_.variants) must not be empty

    record.getCarePlans
      .flatMap(_.medicationRecommendations.getOrElse(List.empty))
      .flatMap(_.supportingVariants) must not be empty

  }


  it must "conform to the JSON schema" in {

    val schema =
      JsonSchemaFactory
        .getInstance(SpecVersion.VersionFlag.V202012)
        .getSchema(
          Schema[MTBPatientRecord]
            .asPlay(Draft12("https://dnpm-dip/mtb/patient-record-schema.json"))
            .pipe(stringify) 
        )
    
    val jsonRecord =
      new ObjectMapper().readTree(
        Gen.of[MTBPatientRecord].next
          .pipe(toJson(_))
          .pipe(stringify)
      )

    val errors =
      schema.validate(jsonRecord)
        .asScala
        .tap(_.foreach(msg => println(msg.getMessage)))

    errors must be (empty)

  }

}
