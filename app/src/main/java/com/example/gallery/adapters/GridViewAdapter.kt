package com.example.gallery.adapters

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.gallery.R
import com.example.gallery.models.GridModal
import com.example.gallery.utils.getImageResourceId
import com.squareup.picasso.Picasso

class GridViewAdapter (private val images: List<GridModal>, private val context: Context) : BaseAdapter() {

    private var layoutInflater: LayoutInflater? = null
    private lateinit var gridImage: ImageView

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view = convertView
        if (layoutInflater == null){
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null){
            view = layoutInflater!!.inflate(R.layout.grid_view,null)
        }

        gridImage = view!!.findViewById(R.id.gridImage)
        val imagePath = images[position].imageView
        //println("In adapter $imagePath")
        Picasso.get().load("file://$imagePath").fit().centerCrop().into(gridImage)


        return view
    }





}