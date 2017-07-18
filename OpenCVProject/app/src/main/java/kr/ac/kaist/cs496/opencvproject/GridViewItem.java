package kr.ac.kaist.cs496.opencvproject;

import android.graphics.Bitmap;

import java.util.Comparator;

/**
 * Created by q on 2017-07-17.
 */

public class GridViewItem  {
    private Bitmap bitmap;
    private int minDist ;
    private int match;
    private String mall;
    private String url;

    //Constructor
    public GridViewItem(String urlString, String mallString){
        this.url = urlString;
        this.mall = mallString;
    }


    public void setBitmap(Bitmap bmap){   bitmap = bmap;    }
    public void setMinDist(int dist) { minDist = dist ;  }
    public void setMatch(int number) {   match = number;    }
    public void setMall(String loc){mall = loc;}
    public void setUrl(String loca) {url = loca;}

    public Bitmap getBitmap() {
        return this.bitmap ;
    }
    public int getMinDist() {
        return this.minDist ;
    }
    public int getMatch() {
        return this.match;
    }

    public String getMall() {
        return this.mall;
    }
    public String getUrl(){
        return this.url;
    }

    // @Override
    // public int compareTo(GridViewItem i) {return minDist.compareTo(i.getMinDist());}

    //Comparator
    public static final Comparator<GridViewItem> INCREASING_COMPARATOR = new Comparator<GridViewItem>() {
        @Override
        public int compare(GridViewItem o1, GridViewItem o2) {
            int dist1 = o1.getMinDist();
            int dist2 = o2.getMinDist();

            int match1 = o1.getMatch();
            int match2 = o2.getMatch();

            //INCREASING

            if(dist1 == dist2){return match2 - match1;}

            return (dist1 - dist2);
        }
    };

}