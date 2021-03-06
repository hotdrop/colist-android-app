package jp.hotdrop.comlis.repository.company

import io.reactivex.Single
import jp.hotdrop.comlis.model.Company
import jp.hotdrop.comlis.model.JobEvaluation
import jp.hotdrop.comlis.model.ReceiveCompany
import jp.hotdrop.comlis.model.Tag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepository @Inject constructor(
        private val localDataSource: CompanyLocalDataSource,
        private val remoteDataSource: CompanyRemoteDataSource,
        private val jobEvaluationLocalDataSource: JobEvaluationLocalDataSource
) {

    fun find(id: Int) =
            localDataSource.find(id)

    fun find(name: String) =
            localDataSource.find(name)

    fun findAll(): Single<List<Company>> =
            localDataSource.findAll()

    fun findByCategory(categoryId: Int): Single<List<Company>> =
            localDataSource.findByCategory(categoryId)

    fun findByTag(companyId: Int): List<Tag> =
            localDataSource.findByTag(companyId)

    fun countByCategory(categoryId: Int) =
            localDataSource.countByCategory(categoryId)

    fun insert(company: Company, fromRemoteRepository: Boolean = false) {
        if(fromRemoteRepository) {
            localDataSource.insertWithRemote(company)
        } else {
            localDataSource.insert(company)
        }
    }

    fun associateTagByCompany(companyId: Int, tags: List<Tag>) {
        localDataSource.associateTagByCompany(companyId, tags)
    }

    fun update(company: Company, fromRemoteRepository: Boolean = false) {
        if(fromRemoteRepository) {
            localDataSource.updateWithRemote(company)
        } else {
            localDataSource.update(company)
        }
    }

    fun updateOverview(company: Company) {
        localDataSource.updateOverview(company)
    }

    fun updateInformation(company: Company) {
        localDataSource.updateInformation(company)
    }

    fun updateBusiness(company: Company) {
        localDataSource.updateBusiness(company)
    }

    fun updateDescription(company: Company) {
        localDataSource.updateDescription(company)
    }

    fun updateFavorite(id: Int, favorite: Int) {
        localDataSource.updateFavorite(id, favorite)
    }

    fun updateAllOrder(companyIds: List<Int>) {
        localDataSource.updateAllOrder(companyIds)
    }

    fun delete(companyId: Int) {
        localDataSource.delete(companyId)
    }

    fun hasAssociateTag(companyId: Int, tagId: Int) =
            localDataSource.hasAssociateTag(companyId, tagId)

    fun exist(name: String) =
            localDataSource.exist(name)

    fun existExclusionId(name: String, id: Int) =
            localDataSource.existExclusionId(name, id)

    fun findJobEvaluation(companyId: Int) =
            jobEvaluationLocalDataSource.find(companyId)

    fun upsertJobEvaluation(jobEvaluation: JobEvaluation) {
        jobEvaluationLocalDataSource.upsert(jobEvaluation)
    }

    fun findAllFromRemote(latestDateEpoch: Long): Single<List<ReceiveCompany>> =
            remoteDataSource.findAll(latestDateEpoch)
}