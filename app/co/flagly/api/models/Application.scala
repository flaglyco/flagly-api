package co.flagly.api.models

import java.time.ZonedDateTime
import java.util.UUID

final case class Application(id: UUID,
                             accountId: UUID,
                             name: String,
                             token: String,
                             createdAt: ZonedDateTime,
                             updatedAt: ZonedDateTime)
