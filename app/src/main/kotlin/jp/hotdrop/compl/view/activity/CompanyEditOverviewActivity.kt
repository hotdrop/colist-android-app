package jp.hotdrop.compl.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import jp.hotdrop.compl.R
import jp.hotdrop.compl.databinding.ActivityCompanyEditOverviewBinding
import jp.hotdrop.compl.util.ColorUtil
import jp.hotdrop.compl.view.fragment.CompanyEditOverviewFragment

class CompanyEditOverviewActivity : BaseActivity() {

    companion object {
        @JvmStatic private val EXTRA_COMPANY_ID = "companyId"
        @JvmStatic private val EXTRA_COLOR_NAME = "colorName"
        fun startForResult(fragment: Fragment, companyId: Int, colorName: String, requestCode: Int) {
            val intent = Intent(fragment.context, CompanyEditOverviewActivity::class.java).apply {
                putExtra(EXTRA_COMPANY_ID, companyId)
                putExtra(EXTRA_COLOR_NAME, colorName)
            }
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityCompanyEditOverviewBinding>(this, R.layout.activity_company_edit_overview)
        getComponent().inject(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = binding.toolbar.title
            it.setDisplayHomeAsUpEnabled(true)
        }

        val colorName = intent.getStringExtra(EXTRA_COLOR_NAME)
        binding.toolbar.background = ColorUtil.getImageCover(colorName, this)

        val companyId = intent.getIntExtra(EXTRA_COMPANY_ID, -1)
        replaceFragment(CompanyEditOverviewFragment.create(companyId), R.id.content_view)
    }
}