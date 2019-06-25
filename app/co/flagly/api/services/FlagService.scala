package co.flagly.api.services

import java.util.UUID

import co.flagly.api.models.{CreateFlag, UpdateFlag}
import co.flagly.api.repositories.FlagRepository
import co.flagly.core.{Flag, FlaglyError}

class FlagService(flagRepository: FlagRepository) {
  def create(applicationId: UUID, createFlag: CreateFlag): Either[FlaglyError, Flag] = flagRepository.create(createFlag.toFlag(applicationId))

  def getAll(applicationId: UUID): Either[FlaglyError, List[Flag]] = flagRepository.getAll(applicationId)

  def get(applicationId: UUID, flagId: UUID): Either[FlaglyError, Option[Flag]] = flagRepository.get(applicationId, flagId)

  def getByName(applicationId: UUID, name: String): Either[FlaglyError, Option[Flag]] = flagRepository.getByName(applicationId, name)

  def update(applicationId: UUID, flagId: UUID, updateFlag: UpdateFlag): Either[FlaglyError, Flag] = flagRepository.update(applicationId, flagId, updateFlag.toUpdatedFlag)

  def delete(applicationId: UUID, flagId: UUID): Either[FlaglyError, Unit] = flagRepository.delete(applicationId, flagId)
}
