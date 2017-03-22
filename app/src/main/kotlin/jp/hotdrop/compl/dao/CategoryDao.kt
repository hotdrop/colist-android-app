package jp.hotdrop.compl.dao

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import jp.hotdrop.compl.model.Category
import jp.hotdrop.compl.model.Category_Relation

object CategoryDao {

    var orma = OrmaHolder.ORMA

    /**
     * 今はGroupNameが重複しない前提としている。あまり良くない・・
     */
    fun find(name: String): Category {
        return categoryRelation().selector().nameEq(name).first()
    }

    fun find(id: Int): Category {
        return categoryRelation().selector().idEq(id).first()
    }

    fun findAll(): List<Category> {
        return categoryRelation().selector()
                .orderByViewOrderAsc()
                .toList()
    }

    /**
     * 試作。いずれ上は消す
     */
    fun findAll2(): Single<List<Category>> {
        return categoryRelation().selector()
                .orderByViewOrderAsc()
                .executeAsObservable()
                .toList()
                .subscribeOn(Schedulers.io())
    }

    fun insert(argName: String, argColorType: String) {
        val category = Category().apply {
            name = argName
            colorType = argColorType
            viewOrder = maxOrder() + 1
        }
        categoryRelation().inserter().execute(category)
    }

    fun update(category: Category) {
        categoryRelation().updater()
                .name(category.name)
                .colorType(category.colorType)
                .idEq(category.id)
                .execute()
    }

    fun updateAllOrder(categories: List<Category>) {
        for((index, category) in categories.withIndex()) {
            categoryRelation().updater()
                    .viewOrder(index)
                    .idEq(category.id)
                    .execute()
        }
    }

    fun exist(name: String): Boolean {
        return !categoryRelation().selector().nameEq(name).isEmpty
    }

    fun exist(name:String, id: Int): Boolean {
        return !categoryRelation().selector().nameEq(name).idNotEq(id).isEmpty
    }

    private fun categoryRelation(): Category_Relation {
        return orma.relationOfCategory()
    }

    private fun maxOrder(): Int {
        return categoryRelation().selector().maxByViewOrder() ?: 0
    }

}