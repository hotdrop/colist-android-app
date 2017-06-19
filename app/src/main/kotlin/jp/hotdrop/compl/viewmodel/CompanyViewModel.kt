package jp.hotdrop.compl.viewmodel

import android.content.Context
import android.support.annotation.ColorRes
import jp.hotdrop.compl.R
import jp.hotdrop.compl.dao.CategoryDao
import jp.hotdrop.compl.dao.CompanyDao
import jp.hotdrop.compl.dao.JobEvaluationDao
import jp.hotdrop.compl.databinding.ItemCompanyBinding
import jp.hotdrop.compl.model.Company
import jp.hotdrop.compl.model.Tag
import jp.hotdrop.compl.util.ColorUtil

class CompanyViewModel(private var company: Company,
                       private val context: Context,
                       private val companyDao: CompanyDao,
                       private val categoryDao: CategoryDao,
                       private val jobEvaluationDao: JobEvaluationDao): ViewModel() {

    private val SALARY_UNIT = context.getString(R.string.label_salary_unit)
    private val SALARY_RANGE_MARK = context.getString(R.string.label_salary_range_mark)
    private val EMPLOYEES_NUM_UNIT = context.getString(R.string.label_employees_num_unit)
    private val JOB_EVALUATION_UNIT = context.getString(R.string.label_job_evaluation_unit)

    lateinit var viewName: String
    lateinit var viewWantedJob: String
    lateinit var viewJobEvaluation: String
    lateinit var viewEmployeesNum: String
    lateinit var viewSalary: String
    var viewFavorite = 0

    lateinit var colorName: String
    lateinit var viewTags: List<Tag>

    init {
        setData(company)
    }

    private fun setData(company: Company) {

        viewName = company.name
        viewWantedJob = company.wantedJob ?: ""
        viewJobEvaluation = "0" + JOB_EVALUATION_UNIT
        viewEmployeesNum = company.employeesNum.toString() + EMPLOYEES_NUM_UNIT
        viewSalary = company.salaryLow.toString() + SALARY_UNIT
        viewFavorite = company.favorite

        colorName = categoryDao.find(company.categoryId).colorType
        viewTags = companyDao.findByTag(company.id).take(5)

        if(company.salaryHigh > 0) {
            viewSalary += SALARY_RANGE_MARK + company.salaryHigh.toString() + SALARY_UNIT
        }

        jobEvaluationDao.find(company.id)?.let {
            var score = 0
            if(it.correctSentence) score += 20
            if(it.developmentEnv) score += 10
            if(it.appeal) score += 20
            if(it.wantSkill) score += 20
            if(it.personImage) score += 10
            if(it.jobOfferReason) score += 20
            viewJobEvaluation = score.toString() + JOB_EVALUATION_UNIT
        }
    }

    fun change(vm: CompanyViewModel) {
        viewName = vm.viewName
        viewWantedJob = vm.viewWantedJob
        viewJobEvaluation = vm.viewJobEvaluation
        viewEmployeesNum = vm.viewEmployeesNum
        viewSalary = vm.viewSalary
        colorName = vm.colorName
        viewFavorite = vm.viewFavorite
        viewTags = vm.viewTags
    }

    @ColorRes
    fun getColorRes(): Int = ColorUtil.getResNormal(colorName, context)

    override fun equals(other: Any?): Boolean = (other as CompanyViewModel).company.id == company.id || super.equals(other)

    fun getId(): Int {
        return company.id
    }

    fun playFavorite(binding: ItemCompanyBinding) {
        resetFavoriteAnimation(binding)
        listOf(binding.animationView1, binding.animationView2, binding.animationView3).take(viewFavorite).forEach {
            it.playAnimation()
        }
    }

    fun onClickFirstFavorite(binding: ItemCompanyBinding) {
        if(viewFavorite == 1) {
            clearFavorite(binding)
        } else {
            binding.animationView1.playAnimation()
            binding.animationView2.reset()
            binding.animationView3.reset()
            viewFavorite = 1
            companyDao.updateFavorite(company.id, viewFavorite)
        }
    }

    fun onClickSecondFavorite(binding: ItemCompanyBinding) {
        if(viewFavorite == 2) {
            clearFavorite(binding)
        } else {
            binding.animationView1.playAnimation()
            binding.animationView2.playAnimation()
            binding.animationView3.reset()
            viewFavorite = 2
            companyDao.updateFavorite(company.id, viewFavorite)
        }
    }

    fun onClickThirdFavorite(binding: ItemCompanyBinding) {
        if(viewFavorite == 3) {
            clearFavorite(binding)
        } else {
            binding.animationView1.playAnimation()
            binding.animationView2.playAnimation()
            binding.animationView3.playAnimation()
            viewFavorite = 3
            companyDao.updateFavorite(company.id, viewFavorite)
        }
    }

    private fun clearFavorite(binding: ItemCompanyBinding) {
        resetFavoriteAnimation(binding)
        updateNonFavorite()
    }

    private fun updateNonFavorite() {
        viewFavorite = 0
        companyDao.updateFavorite(company.id, 0)
    }

    private fun resetFavoriteAnimation(binding: ItemCompanyBinding) {
        binding.animationView1.reset()
        binding.animationView2.reset()
        binding.animationView3.reset()
    }


}