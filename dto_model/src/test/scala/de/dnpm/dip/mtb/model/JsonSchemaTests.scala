package de.dnpm.dip.mtb.model


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


class JsonSchemaTests extends AnyFlatSpec with MTBJsonSchemas
{

  "JSON Schema derivation for MTBPatientRecord" must "have worked" in {

//    Schema[MTBMedicationTherapy].asPlay(Draft04)
    Schema[MTBMedicationTherapy].asPlay(Draft12(""))
      .pipe(prettyPrint(_))
      .tap(println(_))

    succeed   
  }

}
