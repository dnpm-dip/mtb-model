package de.dnpm.dip.mtb.model.json


import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import json.Schema
import json.schema.Version._
import com.github.andyglow.jsonschema.AsPlay._
import play.api.libs.json.Json.prettyPrint
import de.dnpm.dip.mtb.model.MTBPatientRecord


class JsonSchemaTests extends AnyFlatSpec with Schemas
{

  "JSON Schema derivation for MTBPatientRecord" must "have worked" in {

    val schema =
      Schema[MTBPatientRecord].asPlay(Draft12("MTBPatientRecord"))
        .pipe(prettyPrint(_))
//        .tap(println(_))
/*
        .tap { 
          sch =>
            import java.io.FileWriter
            import scala.util.Using

            Using(new FileWriter("/home/lucien/mtb_patient_record_schema.json")){
              _.write(sch)
            }

        }
*/

    schema must not include ("Coding[")
    schema must not include ("head")
    schema must not include ("tail")

  }

}
