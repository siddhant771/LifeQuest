package com.siddhant.lifequest.di




import com.siddhant.lifequest.data.repository.QuestRepositoryImp
import com.siddhant.lifequest.domain.repository.QuestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuestRepository(
        implementation: QuestRepositoryImp
    ): QuestRepository
}