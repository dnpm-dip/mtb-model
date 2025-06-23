package de.dnpm.dip.mtb.model


import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
}
import de.dnpm.dip.model.{
  Id,
  Patient,
  Reference
}
import play.api.libs.json.{
  Json,
  OFormat
}


final case class FamilyMemberHistory
(
  id: Id[FamilyMemberHistory],
  patient: Reference[Patient],
  relationship: Coding[FamilyMemberHistory.RelationshipType.Value]
)


object FamilyMemberHistory
{

  object RelationshipType
  extends CodedEnum("dnpm-dip/mtb/family-meber-history/relationship-type")
  with DefaultCodeSystem
  {
    val FAMMEMB, EXT = Value

    override val display =
      Map(
        FAMMEMB -> "Verwandter ersten Grades",
        EXT     -> "Verwandter weiteren Grades",
      )

  }

  implicit val format: OFormat[FamilyMemberHistory] =
    Json.format[FamilyMemberHistory]

}
