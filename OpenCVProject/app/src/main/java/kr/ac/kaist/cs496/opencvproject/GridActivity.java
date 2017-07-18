package kr.ac.kaist.cs496.opencvproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by q on 2017-07-17.
 */

public class GridActivity extends AppCompatActivity {

    String TAG = "GridActivity ::";

    // references to our images

    ImageAdapter adapter;

    String url = "http://13.124.152.145:4000/clothes/";


    DisplayMetrics mMetrics;

    ArrayList<GridViewItem> temp = new ArrayList<GridViewItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        GridView gridview = (GridView) findViewById(R.id.gridview);

        adapter = new ImageAdapter(getApplicationContext());
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                GridViewItem item = (GridViewItem) adapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getMall()));
                startActivity(intent);
                //Toast.makeText(GridActivity.this, Integer.toString(item.getMinDist()), Toast.LENGTH_SHORT).show();
            }
        });
        //gridview.setOnItemClickListener(gridviewOnItemClickListener);

        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        // null check needed
        url = url + MainActivity.category;

        Log.d(TAG, "onCreate: url? : " + url);

        GetServerTask getServerTask = new GetServerTask(url, null);
        getServerTask.execute();
    }

    //Item Click
    /*
    private GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            GridViewItem item = (GridViewItem) parent.getItemAtPosition(position);
            //GridViewItem item = (GridViewItem) .getItem(position);

            //long viewid = view.getId();

            Toast.makeText(GridActivity.this, position, Toast.LENGTH_SHORT).show();
        }
    };
    */

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<GridViewItem> gridViewItemList = new ArrayList<GridViewItem>();

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return gridViewItemList.size();
        }

        public Object getItem(int position) {
            return gridViewItemList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            /*

            int rowWidth = (mMetrics.widthPixels) / 3;

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(rowWidth,rowWidth));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(1, 1, 1, 1);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(gridViewItemList.get(position).getBitmap());
            return imageView;

            */
            GridItemView view = new GridItemView(getApplicationContext());
            GridViewItem item = gridViewItemList.get(position);
            view.setImage(item.getBitmap());

            return view;
        }

        public void add(GridViewItem item){
            gridViewItemList.add(item);
            Collections.sort(gridViewItemList, GridViewItem.INCREASING_COMPARATOR);
        }

        /*


        public void addItem(JSONArray clothes) {
            try {
                for (int i=0; i<clothes.length(); i++) {
                    GridViewItem item = new GridViewItem();
                    JSONObject cloth = clothes.getJSONObject(i);
                    Log.d(TAG, cloth.toString());

                    try {
                        URL imageurl = new URL("http://13.124.152.145:4000/"+cloth.optString("path").replace(" ","%20"));
                        Log.d(TAG, "image urlpath"  + imageurl);
                        URLConnection conn = imageurl.openConnection();
                        conn.connect();

                        int nSize = conn.getContentLength();
                        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(),nSize);
                        item.setBitmap(BitmapFactory.decodeStream(bis));
                        bis.close();
                        //item.setBitmap(BitmapFactory.decodeStream(imageurl.openConnection().getInputStream()));
                        Log.d(TAG, " GET IMAGE  ");
                    }catch(IOException e){
                        Log.d(TAG, " GET IMAGE ERROR ");
                        System.out.println(e);
                    }
                    List<Integer> list;

                    item.setMall(cloth.optString("shoppingmall"));
                    list = compareFeature(MainActivity.mBitmap,item.getBitmap());

                    item.setMinDist(list.get(0));
                    item.setMatch(list.get(1));

                    gridViewItemList.add(item);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Collections.sort(gridViewItemList, GridViewItem.DESCENDING_COMPARATOR);

        }
        */

    }

    public class GetServerTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        JSONArray clothesList;

        public GetServerTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            try {
                result = requestHttpURLConnection.getFromServer(url, values); // 해당 URL로 부터 결과물을 얻어온다.
                return result;
            }catch (IOException e) { // for openConnection().
                Log.d(TAG, " BACK GROUND ERROR ");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                clothesList = new JSONArray(s);
                Log.d(TAG, clothesList.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i=0; i<clothesList.length(); i++) {
                try {
                    JSONObject cloth = clothesList.getJSONObject(i);
                    GridViewItem item = new GridViewItem("empty","empty");
                    item.setMall(cloth.optString("shoppingmall"));
                    item.setUrl(cloth.optString("path"));
                    temp.add(item);
                    Log.d(TAG, "doinback : " + temp.get(i).getUrl());
                    GetUrlTask getUrlTask = new GetUrlTask("http://13.124.152.145:4000/"+temp.get(i).getUrl().replace(" ", "%20"),temp.get(i));
                    getUrlTask.execute();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            //adapter.addItem(clothesList);
            //adapter.notifyDataSetChanged();
        }
    }

    public class GetUrlTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private GridViewItem values;

        public GetUrlTask(String url, GridViewItem values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                return myBitmap;
            }catch(IOException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            super.onPostExecute(bmp);

            Bitmap bm = bmp;
            values.setBitmap(bm);

            List<Integer> list;
            list = compareFeature(MainActivity.mBitmap,values.getBitmap());


            Log.d(TAG, "Mindist : " + list.get(0));

            Log.d(TAG, "Match  : " + list.get(1));

            values.setMinDist(list.get(0));
            values.setMatch(list.get(1));

            //if(values.getMinDist() <= 20) {
                adapter.add(values);
                adapter.notifyDataSetChanged();
            //}
        }
    }

    public static List<Integer> compareFeature(Bitmap bmp1, Bitmap bmp2) {
        int retVal = 0;
        List<Integer> list = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // Load images to compare
        //Mat img1 = Imgcodecs.imread(filename1, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        //Mat img2 = Imgcodecs.imread(filename2, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        //Log.i(TAG, context.getResources().getDrawable(R.drawable.image1).toString());
        //String path = Uri.parse("android.resource://kr.ac.kaist.cs496.opencvproject/drawable/image1.jpg").toString();

        //Log.i(TAG, path);
        Mat img1 = new Mat();
        Mat img2 = new Mat();

        // Log.i(TAG, bmp1.toString());


        Utils.bitmapToMat(bmp1, img1);
        Utils.bitmapToMat(bmp2, img2);


        //Mat imgone = Utils.loadResource(context,context.getResources().getIdentifier("image1","drawable","kr.ac.kaist.cs496.opencvproject"),Highgui.CV_LOAD_IMAGE_COLOR);
        //Mat img1 = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        //Mat img2 = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        //Mat img1 = Imgcodecs.imread(context.getResources().getDrawable(R.drawable.image1).toString(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
        //Mat img2 = Imgcodecs.imread(context.getResources().getDrawable(R.drawable.image1).toString(), Imgcodecs.CV_LOAD_IMAGE_COLOR);

        // Declare key point of images
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        // Definition of ORB key point detector and descriptor extractors
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        // Detect key points
        detector.detect(img1, keypoints1);
        detector.detect(img2, keypoints2);

        // Extract descriptors
        extractor.compute(img1, keypoints1, descriptors1);
        extractor.compute(img2, keypoints2, descriptors2);

        // Definition of descriptor matcher
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        // Match points of two images
        MatOfDMatch matches = new MatOfDMatch();
//  System.out.println("Type of Image1= " + descriptors1.type() + ", Type of Image2= " + descriptors2.type());
//  System.out.println("Cols of Image1= " + descriptors1.cols() + ", Cols of Image2= " + descriptors2.cols());

        // Avoid to assertion failed
        // Assertion failed (type == src2.type() && src1.cols == src2.cols && (type == CV_32F || type == CV_8U)
        int min_dist = 100;
        if (descriptors2.cols() == descriptors1.cols()) {
            matcher.match(descriptors1, descriptors2, matches);

            // Check matches of key points
            DMatch[] match = matches.toArray();
            int max_dist = 0;
            //double min_dist = 100;

            for (int i = 0; i < descriptors1.rows(); i++) {
                int dist = (int) match[i].distance;
                if (dist < min_dist) min_dist = dist;
                if (dist > max_dist) max_dist = dist;
            }
            System.out.println("max_dist=" + max_dist + ", min_dist=" + min_dist);

            // Extract good images (distances are under 10)
            for (int i = 0; i < descriptors1.rows(); i++) {
                if (match[i].distance <= 20) {
                    retVal++;
                }
            }
            System.out.println("matching count=" + retVal);
        }

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("estimatedTime=" + estimatedTime + "ms");

        //palette
        Palette p1 = Palette.from(bmp1).generate();
        Palette p2 = Palette.from(bmp2).generate();

        //Dominant
        int c1 = p1.getDominantColor(0);
        int c2 = p2.getDominantColor(0);

        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        //VirantColor
        int vc1 = p1.getVibrantColor(0);
        int vc2 = p2.getVibrantColor(0);

        int vr1 = (vc1 >> 16) & 0xFF;
        int vg1 = (vc1 >> 8) & 0xFF;
        int vb1 = vc1 & 0xFF;

        int vr2 = (vc2 >> 16) & 0xFF;
        int vg2 = (vc2 >> 8) & 0xFF;
        int vb2 = vc2 & 0xFF;

        //DarkVirant

        int dvc1 = p1.getDarkVibrantColor(0);
        int dvc2 = p2.getDarkVibrantColor(0);

        int dvr1 = (dvc1 >> 16) & 0xFF;
        int dvg1 = (dvc1 >> 8) & 0xFF;
        int dvb1 = dvc1 & 0xFF;

        int dvr2 = (dvc2 >> 16) & 0xFF;
        int dvg2 = (dvc2 >> 8) & 0xFF;
        int dvb2 = dvc2 & 0xFF;


        int colorDist = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
        int virantDist = Math.abs(vr1 - vr2) + Math.abs(vg1 - vg2) + Math.abs(vb1 - vb2);
        int DarkDist = Math.abs(dvr1 - dvr2) + Math.abs(dvg1 - dvg2) + Math.abs(dvb1 - dvb2);




        //list.add((int)min_dist);

        list.add(colorDist+min_dist*2+(virantDist+DarkDist)/2);
        list.add(retVal);


        return list;
    }

}
