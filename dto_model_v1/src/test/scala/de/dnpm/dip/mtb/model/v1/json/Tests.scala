package de.dnpm.dip.mtb.model.v1.json


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import json.Schema
import json.schema.Version._
import com.github.andyglow.jsonschema.AsPlay._
import de.dnpm.dip.mtb.model.v1.MTBPatientRecord


class JsonSchemaTests extends AnyFlatSpec with Schemas
{

  "JSON Schema derivation for MTBPatientRecord" must "have worked" in {

    Schema[MTBPatientRecord].asPlay(Draft12("MTBPatientRecord"))
//      .pipe(prettyPrint(_))
//      .tap(println(_))

    succeed   
  }

}
