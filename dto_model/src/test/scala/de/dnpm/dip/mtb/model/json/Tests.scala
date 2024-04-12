package de.dnpm.dip.mtb.model.json


import java.time.LocalDate
import scala.util.chaining._
import cats.data.NonEmptyList
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import json.Schema
import json.schema.Version._
import com.github.andyglow.jsonschema.AsPlay._
import play.api.libs.json.Json.{
  prettyPrint,
  toJson
}
import de.dnpm.dip.model.{
  Patient,
  Period,
  OpenEndPeriod
}
import de.dnpm.dip.mtb.model._


class JsonSchemaTests extends AnyFlatSpec with Schemas
{

  "JSON Schema derivation for MTBPatientRecord" must "have worked" in {

    Schema[MTBPatientRecord].asPlay(Draft12("MTBPatientRecord"))
//      .pipe(prettyPrint(_))
//      .tap(println(_))

    succeed   
  }

}
