package com.github.makiftutuncu.switchboard

import java.util.UUID

final case class Flag[V](id: UUID,
                         name: String,
                         description: String,
                         value: V,
                         defaultValue: V)

object Flag {
  def apply[V](name: String,
               description: String,
               value: V,
               defaultValue: V): Flag[V] =
    new Flag[V](
      UUID.randomUUID,
      name,
      description,
      value,
      defaultValue
    )
}
