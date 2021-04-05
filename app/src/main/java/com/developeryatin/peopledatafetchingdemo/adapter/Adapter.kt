package com.developeryatin.peopledatafetchingdemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.developeryatin.peopledatafetchingdemo.model.PeoplesData
import com.developeryatin.peopledatafetchingdemo.R
import com.developeryatin.peopledatafetchingdemo.adapter.Adapter.MyViewHolder
import com.google.android.material.button.MaterialButton
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Adapter(private val articles: List<PeoplesData>?, private val context: Context) :
    RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onBindViewHolder(holders: MyViewHolder, position: Int) {
        val model = articles!![position]

        Glide.with(context).load(5.avatarImageGenerate(context, model.name))
            .into(holders.imageView)

        holders.title.text = model.name
        holders.desc.text = model.email
        holders.publishedAt.text = dateFormat(model.created_at)
        holders.author.text = model.status
        if (model.name != "") holders.centerText.text = model.name.substringBefore(" ")
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateFormat(oldStringDate: String?): String? {
        val newDate: String?
        val dateFormat = SimpleDateFormat("E, d MMM yyyy", Locale.getDefault())
        newDate = try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(oldStringDate)
            dateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            oldStringDate
        }
        return newDate
    }

    override fun getItemCount(): Int {
        return articles!!.size
    }

    inner class MyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var desc: TextView = itemView.findViewById(R.id.desc)
        var author: TextView = itemView.findViewById(R.id.author)
        var publishedAt: TextView = itemView.findViewById(R.id.publishedAt)
        var imageView: ImageView = itemView.findViewById(R.id.img)
        var centerText: MaterialButton = itemView.findViewById(R.id.centerText)

    }

    private fun Int.avatarImageGenerate(
        context: Context,
        name: String
    ): BitmapDrawable {

        val label: String = firstCharacter(name)
        val textPaint = textPainter(context)
        val painter = painter()
        painter.isAntiAlias = true
        val areaRect = Rect(0, 0, this, this)
        painter.color = Color.TRANSPARENT
        val bitmap = Bitmap.createBitmap(this, this, ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRect(areaRect, painter)
        val range = 10 until 100
        val colorString = "#${range.random()}${range.random()}${range.random()}"
        val color = Color.parseColor(colorString)
        painter.color = color

        val bounds = RectF(areaRect)
        bounds.right = textPaint.measureText(label, 0, 1)
        bounds.bottom = textPaint.descent() - textPaint.ascent()

        bounds.left += (areaRect.width() - bounds.right) / 2.0f
        bounds.top += (areaRect.height() - bounds.bottom) / 2.0f

        canvas.drawCircle(toFloat() / 2, toFloat() / 2, toFloat() / 2, painter)
        label.let { canvas.drawText(it, bounds.left, bounds.top - textPaint.ascent(), textPaint) }
        return BitmapDrawable(context.resources, bitmap)

    }

    private fun firstCharacter(name: String?): String {
        return if (name == "") {
            " "
        } else name?.first().toString().toUpperCase(Locale.ROOT)
    }

    private fun textPainter(context: Context): TextPaint {
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 0f * context.resources.displayMetrics.scaledDensity
        textPaint.color = Color.WHITE
        return textPaint
    }

    private fun painter(): Paint {
        return Paint()
    }

    /*private fun calTextSize(size: Int): Float {
        return (size / 4.5).toFloat()
    }*/
}