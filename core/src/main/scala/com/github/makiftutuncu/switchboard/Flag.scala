package com.github.makiftutuncu.switchboard

import java.util.UUID

final case class Flag[V](id: UUID, name: String, description: String, value: V)
