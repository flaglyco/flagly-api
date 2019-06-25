package co.flagly.api.services

import java.util.UUID

import co.flagly.api.models.{CreateFlag, UpdateFlag}
import co.flagly.api.repositories.FlagRepository
import co.flagly.core.{Flag, FlaglyError}

class FlagService(flagRepository: FlagRepository) {
  def create(createFlag: CreateFlag): Either[FlaglyError, Flag] = flagRepository.create(createFlag.toFlag)

  def getAll: Either[FlaglyError, List[Flag]] = flagRepository.getAll

  def get(id: UUID): Either[FlaglyError, Option[Flag]] = flagRepository.get(id)

  def getByName(name: String): Either[FlaglyError, Option[Flag]] = flagRepository.getByName(name)

  def update(id: UUID, updateFlag: UpdateFlag): Either[FlaglyError, Flag] = flagRepository.update(id, updateFlag.toUpdatedFlag)

  def delete(id: UUID): Either[FlaglyError, Unit] = flagRepository.delete(id)
}
