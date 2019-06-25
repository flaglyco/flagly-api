package co.flagly.api.models

import java.time.ZonedDateTime
import java.util.UUID

final case class Account(id: UUID,
                         name: String,
                         email: String,
                         password: String,
                         salt: String,
                         createdAt: ZonedDateTime,
                         updatedAt: ZonedDateTime)
