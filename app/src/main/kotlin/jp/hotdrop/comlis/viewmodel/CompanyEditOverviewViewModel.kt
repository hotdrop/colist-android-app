package jp.hotdrop.comlis.viewmodel

import android.content.Context
import android.support.annotation.ColorRes
import io.reactivex.Completable
import io.reactivex.Single
import jp.hotdrop.comlis.model.Company
import jp.hotdrop.comlis.repository.category.CategoryRepository
import jp.hotdrop.comlis.repository.company.CompanyRepository
import jp.hotdrop.comlis.util.ColorUtil
import javax.inject.Inject

class CompanyEditOverviewViewModel @Inject constructor(
        private val context: Context,
        private val companyRepository: CompanyRepository,
        private val categoryRepository: CategoryRepository
): ViewModel() {

    lateinit var viewName: String
    lateinit var viewOverview: String
    lateinit var colorName: String

    private var companyId: Int = -1
    var categoryId: Int = -1

    var isChangeCategory: Boolean = false

    fun loadData(companyId: Int): Completable =
            Single.just(companyRepository.find(companyId))
                    .flatMapCompletable { company ->
                        setData(company)
                        Completable.complete()
                    }

    private fun setData(company: Company) {
        viewName = company.name
        viewOverview = company.overview ?: ""
        colorName = categoryRepository.find(company.categoryId).colorType
        companyId = company.id
        categoryId = company.categoryId
    }

    @ColorRes
    fun getColorRes() =
            ColorUtil.getResDark(colorName, context)

    fun existName(name: String) =
        if(name.isBlank())
            false
        else
            companyRepository.existExclusionId(name, companyId)

    fun update(selectedCategorySpinnerId: Int) {
        isChangeCategory = (categoryId != selectedCategorySpinnerId)

        val company =  makeCompany(selectedCategorySpinnerId)
        companyRepository.updateOverview(company)
    }

    private fun makeCompany(selectedCategorySpinnerId: Int) = Company().apply {
        id = companyId
        name = viewName
        categoryId = selectedCategorySpinnerId
        overview = viewOverview.toStringOrNull()
    }
}