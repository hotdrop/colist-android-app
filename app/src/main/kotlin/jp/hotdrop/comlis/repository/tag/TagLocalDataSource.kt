package jp.hotdrop.comlis.repository.tag

import io.reactivex.Single
import jp.hotdrop.comlis.model.Tag
import jp.hotdrop.comlis.repository.OrmaHolder
import java.util.*
import javax.inject.Inject

class TagLocalDataSource @Inject constructor(
        ormaHolder: OrmaHolder
) {

    private val orma = ormaHolder.orma

    fun find(name: String): Tag? =
            tagRelation().selector()
                    .nameEq(name)
                    .valueOrNull()

    fun findInIds(ids: List<Int>): List<Tag> =
            tagRelation().selector()
                    .idIn(ids)
                    .orderByViewOrderAsc()
                    .toList()

    fun findAll(): Single<List<Tag>> =
            tagRelation().selector()
                    .orderByViewOrderAsc()
                    .executeAsObservable()
                    .toList()


    fun countByAttachCompany(tag: Tag) =
            associateCompanyAndTagRelation().selector()
                    .tagIdEq(tag.id)
                    .count()

    fun insert(argTag: Tag) {
        val tag = Tag().apply {
            name = argTag.name
            colorType = argTag.colorType
            viewOrder = maxOrder() + 1
            registerDate = Date(System.currentTimeMillis())
        }
        orma.transactionSync {
            tagRelation().inserter().execute(tag)
        }
    }

    fun update(tag: Tag) {
        orma.transactionSync {
            tagRelation().updater()
                    .name(tag.name)
                    .colorType(tag.colorType)
                    .updateDate(Date(System.currentTimeMillis()))
                    .idEq(tag.id)
                    .execute()
        }
    }

    fun updateAllOrder(tags: List<Tag>) {
        orma.transactionSync {
            tags.forEachIndexed { index, tag ->
                tagRelation().updater()
                        .viewOrder(index)
                        .idEq(tag.id)
                        .execute()
            }
        }
    }

    fun delete(tag: Tag) {
        orma.transactionSync {
            tagRelation().deleter()
                    .idEq(tag.id)
                    .execute()
        }
    }

    fun exist(name: String) =
            !(tagRelation().selector()
                    .nameEq(name)
                    .isEmpty)

    fun existExclusionId(name: String, id: Int) =
            !(tagRelation().selector()
                    .nameEq(name)
                    .idNotEq(id)
                    .isEmpty)

    private fun maxOrder() =
            tagRelation().selector()
                    .maxByViewOrder() ?: 0

    private fun tagRelation() =
            orma.relationOfTag()

    private fun associateCompanyAndTagRelation() =
            orma.relationOfAssociateCompanyWithTag()

}