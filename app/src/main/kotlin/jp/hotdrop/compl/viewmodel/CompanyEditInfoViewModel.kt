package jp.hotdrop.compl.viewmodel

import android.content.Context
import android.support.annotation.ColorRes
import io.reactivex.Completable
import io.reactivex.rxkotlin.toSingle
import jp.hotdrop.compl.model.Company
import jp.hotdrop.compl.repository.category.CategoryRepository
import jp.hotdrop.compl.repository.company.CompanyRepository
import jp.hotdrop.compl.util.ColorUtil
import javax.inject.Inject

class CompanyEditInfoViewModel @Inject constructor(val context: Context): ViewModel() {

    @Inject
    lateinit var companyRepository: CompanyRepository
    @Inject
    lateinit var categoryRepository: CategoryRepository

    lateinit var viewEmployeesNum: String
    lateinit var viewSalaryLow: String
    lateinit var viewSalaryHigh: String
    lateinit var viewWantedJob: String
    lateinit var viewWorkPlace: String
    lateinit var viewUrl: String
    lateinit var colorName: String

    private var companyId: Int = -1

    fun loadData(companyId: Int): Completable =
            companyRepository.find(companyId)
                    .toSingle()
                    .flatMapCompletable { company ->
                        setData(company)
                        Completable.complete()
                    }

    private fun setData(company: Company) {
        viewEmployeesNum = if(company.employeesNum > 0) company.employeesNum.toString() else ""
        viewSalaryLow = if(company.salaryLow > 0) company.salaryLow.toString() else ""
        viewSalaryHigh = if(company.salaryHigh > 0) company.salaryHigh.toString() else ""
        viewWantedJob = company.wantedJob ?: ""
        viewWorkPlace = company.workPlace ?: ""
        viewUrl = company.url ?: ""
        colorName = categoryRepository.find(company.categoryId).colorType
        companyId = company.id
    }

    @ColorRes
    fun getColorRes() = ColorUtil.getResDark(colorName, context)

    fun update() {
        val company =  makeCompany()
        companyRepository.updateInformation(company)
    }

    private fun makeCompany() = Company().apply {
        id = companyId
        employeesNum = viewEmployeesNum.toIntOrZero()
        salaryLow = viewSalaryLow.toIntOrZero()
        salaryHigh = viewSalaryHigh.toIntOrZero()
        wantedJob = viewWantedJob.toStringOrNull()
        workPlace = viewWorkPlace .toStringOrNull()
        url = viewUrl.toStringOrNull()
    }
}