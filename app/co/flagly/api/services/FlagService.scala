package co.flagly.api.services

import java.util.UUID

import co.flagly.api.FlagRepository
import co.flagly.api.errors.FlaglyError
import co.flagly.api.models.{CreateFlag, Flag, UpdateFlag}
import javax.inject.{Inject, Singleton}

@Singleton
class FlagService @Inject()(flagRepository: FlagRepository) {
  def create(createFlag: CreateFlag): Either[FlaglyError, Flag] = flagRepository.create(createFlag.toFlag)

  def getAll: Either[FlaglyError, List[Flag]] = flagRepository.getAll

  def get(id: UUID): Either[FlaglyError, Option[Flag]] = flagRepository.get(id)

  def update(id: UUID, updateFlag: UpdateFlag): Either[FlaglyError, Flag] = flagRepository.update(id, updateFlag.toUpdatedFlag)

  def delete(id: UUID): Either[FlaglyError, Unit] = flagRepository.delete(id)
}
