package jp.hotdrop.comlis.view.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import jp.hotdrop.comlis.R
import jp.hotdrop.comlis.databinding.ActivitySearchBinding
import jp.hotdrop.comlis.view.fragment.SearchFragment

class SearchActivity: BaseActivity() {

    companion object {
        fun start(fragment: Fragment) {
            val intent = Intent(fragment.context, SearchActivity::class.java)
            fragment.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_company_detail)
        val binding = DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search)

        getComponent().inject(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        replaceFragment(SearchFragment.create(), R.id.content_view)
    }
}