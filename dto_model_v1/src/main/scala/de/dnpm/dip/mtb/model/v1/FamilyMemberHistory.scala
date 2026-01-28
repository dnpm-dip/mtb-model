package de.dnpm.dip.mtb.model.v1


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.{
  Id,
  Patient
}
import de.dnpm.dip.mtb.model.FamilyMemberHistory.RelationshipType
import play.api.libs.json.{
  Json,
  OFormat
}


final case class FamilyMemberHistory
(
  id: Id[FamilyMemberHistory],
  patient: Id[Patient],
  relationship: Coding[RelationshipType.Value]
)

object FamilyMemberHistory
{
  implicit val format: OFormat[FamilyMemberHistory] =
    Json.format[FamilyMemberHistory]
}

