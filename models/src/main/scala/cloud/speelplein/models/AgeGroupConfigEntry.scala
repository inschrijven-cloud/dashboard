package cloud.speelplein.models

object AgeGroupConfigEntry {
  type Id = String
}

case class AgeGroupConfigEntry(id: AgeGroupConfigEntry.Id,
                               name: String,
                               bornOnOrAfter: DayDate,
                               bornOnOrBefore: DayDate)

case class AgeGroupConfig(groups: Seq[AgeGroupConfigEntry])
