package cloud.speelplein.models

case class AgeGroupConfigEntry(name: String, // Identifies the AgeGroup
                               bornOnOrAfter: DayDate,
                               bornOnOrBefore: DayDate)

case class AgeGroupConfig(groups: Seq[AgeGroupConfigEntry])
