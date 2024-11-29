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
        .tap(println(_))

    schema must not contain ("Coding[")
    schema must contain noneOf ("head","tail")

  }

}
