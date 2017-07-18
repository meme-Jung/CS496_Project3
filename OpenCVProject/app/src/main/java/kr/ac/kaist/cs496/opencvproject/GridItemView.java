package kr.ac.kaist.cs496.opencvproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by q on 2017-07-17.
 */

public class GridItemView extends LinearLayout{

    ImageView imageView;

    public GridItemView(Context context){
        super(context);
        init(context);
    }
    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.gridview_item, this, true);

        imageView = (ImageView) findViewById(R.id.imageView1);
    }

    public void setImage(Bitmap bmap){
        imageView.setImageBitmap(bmap);
    }

}
