package jp.hotdrop.compl.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Table

@Table
class RelationCompanyAndTag {

    @PrimaryKey(autoincrement = true)
    var id: Int = 0

    @Column(indexed = true)
    var companyId: Int = 0

    @Column(indexed = true)
    var tagId: Int = 0
}