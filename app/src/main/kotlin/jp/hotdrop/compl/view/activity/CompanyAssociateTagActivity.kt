package jp.hotdrop.compl.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import jp.hotdrop.compl.R
import jp.hotdrop.compl.databinding.ActivityCompanyAssociateTagBinding
import jp.hotdrop.compl.util.ColorUtil
import jp.hotdrop.compl.view.fragment.CompanyAssociateTagFragment

class CompanyAssociateTagActivity: BaseActivity() {

    companion object {
        @JvmStatic private val EXTRA_COMPANY_ID = "companyId"
        @JvmStatic private val EXTRA_COLOR_NAME = "colorName"
        fun startForResult(fragment: Fragment, companyId: Int, colorName: String, requestCode: Int) {
            val intent = Intent(fragment.context, CompanyAssociateTagActivity::class.java).apply {
                putExtra(EXTRA_COMPANY_ID, companyId)
                putExtra(EXTRA_COLOR_NAME, colorName)
            }
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityCompanyAssociateTagBinding>(this, R.layout.activity_company_associate_tag)
        getComponent().inject(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = binding.toolbar.title
            it.setDisplayHomeAsUpEnabled(true)
        }

        val companyId = intent.getIntExtra(EXTRA_COMPANY_ID, -1)
        val colorName = intent.getStringExtra(EXTRA_COLOR_NAME)
        binding.toolbar.setBackgroundColor(ColorUtil.getResDark(colorName, this))
        replaceFragment(CompanyAssociateTagFragment.create(companyId, colorName), R.id.content_view)
    }
}