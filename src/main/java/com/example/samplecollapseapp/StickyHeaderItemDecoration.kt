package com.example.samplecollapseapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class StickyHeaderItemDecoration(val mListener: StickyHeaderInterface) :
    RecyclerView.ItemDecoration() {
    private var currentHeader: View? = null

    // RecyclerViewのセルが表示されたときに呼ばれる
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        // 一番上のビュー
        val topChild = parent.getChildAt(0) ?: return  // RecyclerViewの中身がない
        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }
        val prevHeaderPosition = mListener.getHeaderPositionForItem(topChildPosition)
        if (prevHeaderPosition == -1) {
            return
        }
        // ヘッダービューが表示された
        getHeaderViewForItem(topChildPosition, parent)?.let { currentHeader ->
            this.currentHeader = currentHeader
            fixLayoutSize(parent, currentHeader)
            currentHeader.bottom?.let { contactPoint ->
                // 次のセルを取得
                val childInContact = getChildInContact(parent, contactPoint) ?: return  // 次のセルがない
                // ヘッダーの判定
                if (mListener.isHeader(parent.getChildAdapterPosition(childInContact))) { // 既存のStickyヘッダーを押し上げる
                    moveHeader(c, currentHeader, childInContact)
                    return
                }
                // Stickyヘッダーの描画
                drawHeader(c, currentHeader)
            }
        }
    }

    // dp <=> pixel変換
    fun convertDp2Px(dp: Float, context: Context): Float {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        return dp * metrics.density
    }


    // Stickyヘッダービューの取得
    private fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View {
        val headerPosition = mListener.getHeaderPositionForItem(itemPosition)
        val layoutResId = mListener.getHeaderLayout(headerPosition)
        // Stickyヘッダーレイアウトをinflateする
        val header = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        //header.setElevation(header,convertDp2Px(R.dimen.shadow,header.getContext()));
        // Stickyレイアウトにデータバインドする
        mListener.bindHeaderData(header, headerPosition)
        return header
    }

    // Stickyヘッダーを描画する
    private fun drawHeader(c: Canvas, header: View) {
        c.save()
        c.translate(0.0f, 0.0f)
        header.draw(c)
        drawShadow(header, c)
        c.restore()
    }

    // Stickyヘッダーを動かす
    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.save()
        c.translate(0.0f, nextHeader.top.toFloat() - currentHeader.height.toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun drawShadow(target: View, c: Canvas) {
        val paint = Paint()
        paint.setShadowLayer(10.0f, 0.0f, 2.0f, -0x1000000)
        val layoutParams = target.layoutParams
        c.drawRect(0.0f, 0.0f, layoutParams.width.toFloat(), layoutParams.height.toFloat(), paint)
    }

    // 座標から次のRecyclerViewのセル位置を取得
    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.bottom > contactPoint) {
                if (child.top <= contactPoint) {
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    // Stickyヘッダーのレイアウトサイズを取得
    private fun fixLayoutSize(
        parent: ViewGroup,
        view: View?
    ) { // RecyclerViewのSpec
        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)
        // headersのSpec
        view?.let {  v ->
            val childWidthSpec = ViewGroup.getChildMeasureSpec(
                widthSpec,
                parent.paddingLeft + parent.paddingRight,
                v.layoutParams.width
            )
            val childHeightSpec = ViewGroup.getChildMeasureSpec(
                heightSpec,
                parent.paddingTop + parent.paddingBottom,
                v.layoutParams.height
            )
            v.measure(childWidthSpec, childHeightSpec)
            v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        }
    }

    // Stickyヘッダーインタフェース
    interface StickyHeaderInterface {
        fun getHeaderPositionForItem(itemPosition: Int): Int
        fun getHeaderLayout(headerPosition: Int): Int
        fun bindHeaderData(header: View?, headerPosition: Int)
        fun isHeader(itemPosition: Int): Boolean
    }
}