package co.flagly.api.services

import java.util.UUID

import co.flagly.api.errors.FlaglyError
import co.flagly.api.models.{CreateFlag, UpdateFlag}
import co.flagly.api.repositories.FlagRepository
import co.flagly.core.Flag
import javax.inject.{Inject, Singleton}

@Singleton
class FlagService @Inject()(flagRepository: FlagRepository) {
  def create(createFlag: CreateFlag): Either[FlaglyError, Flag] =
    createFlag.toFlag match {
      case None       => Left(FlaglyError.InvalidCreateFlag)
      case Some(flag) => flagRepository.create(flag)
    }

  def getAll: Either[FlaglyError, List[Flag]] = flagRepository.getAll

  def get(id: UUID): Either[FlaglyError, Option[Flag]] = flagRepository.get(id)

  def update(id: UUID, updateFlag: UpdateFlag): Either[FlaglyError, Flag] = flagRepository.update(id, updateFlag.toUpdatedFlag)

  def delete(id: UUID): Either[FlaglyError, Unit] = flagRepository.delete(id)
}
