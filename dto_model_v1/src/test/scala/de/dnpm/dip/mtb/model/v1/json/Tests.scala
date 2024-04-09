package de.dnpm.dip.mtb.model.v1.json


import java.time.LocalDate
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import json.Schema
import json.schema.Version._
import com.github.andyglow.jsonschema.AsPlay._
import play.api.libs.json.Json.{
  prettyPrint,
  toJson
}
import de.dnpm.dip.mtb.model.v1._


class JsonSchemaTests extends AnyFlatSpec with Schemas
{

  "JSON Schema derivation for MTBPatientRecord" must "have worked" in {

    Schema[MTBPatientRecord].asPlay(Draft12("MTBPatientRecord"))
//      .pipe(prettyPrint(_))
//      .tap(println(_))

    succeed   
  }

}
