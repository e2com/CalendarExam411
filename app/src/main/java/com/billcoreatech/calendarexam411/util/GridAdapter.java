package com.billcoreatech.calendarexam411.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.billcoreatech.calendarexam411.R;
import com.billcoreatech.calendarexam411.database.DBHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GridAdapter extends BaseAdapter {

    String TAG = "GridAdapter" ;
    ArrayList<String> list;
    Calendar mCal;
    LayoutInflater inflater;
    TextView tvItemGridView ;
    TextView tv1 ;
    ImageView imageView ;
    LinearLayout ldToday ;
    private int nListCnt = 0;
    SimpleDateFormat sdf ;
    DBHandler dbHandler ;

    /**
     * 생성자
     *
     * @param context
     * @param list
     */
    public GridAdapter(Context context, ArrayList<String> list) {
        this.list = list;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sdf = new SimpleDateFormat("yyyyMMdd") ;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateReceiptsList(ArrayList<String> _oData) {
        list = _oData;
        nListCnt = list.size(); // 배열 사이즈 다시 확인
        this.notifyDataSetChanged(); // 그냥 여기서 하자
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.itemcalendar, parent, false);
            ldToday = convertView.findViewById(R.id.ldToday);
            tvItemGridView = convertView.findViewById(R.id.tv_item_gridview);
            tv1 = convertView.findViewById(R.id.tv1) ;
            imageView = convertView.findViewById(R.id.imageView);
        }
        if (getItem(position).length() > 3) {
            tvItemGridView.setText("" + getItem(position).substring(6, 8));
        } else {
            tvItemGridView.setText("" + getItem(position));
        }

        //해당 날짜 텍스트 컬러,배경 변경
        mCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
        try {
            long now = System.currentTimeMillis();
            Date toDay = new Date(now);
            String sToday = sdf.format(toDay) ;
            mCal.setTime(sdf.parse(getItem(position)));
            Integer weekOfDay = mCal.get(Calendar.DAY_OF_WEEK);

            if (weekOfDay == Calendar.SUNDAY) {
                tvItemGridView.setTextColor(context.getColor(R.color.softred));
            }
            if (weekOfDay == Calendar.SATURDAY) {
                tvItemGridView.setTextColor(context.getColor(R.color.softblue));
            }
            if (sToday.equals(getItem(position))) { //오늘 day 텍스트 컬러 변경
                tvItemGridView.setTextColor(context.getColor(R.color.white));
                ldToday.setBackgroundColor(context.getColor(R.color.softblue));
            }

        } catch (Exception e) {
            if ("일".equals(getItem(position))) {
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white));
                tvItemGridView.setBackgroundColor(context.getColor(R.color.softred));
            } else if ("토".equals(getItem(position))) {
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white));
                tvItemGridView.setBackgroundColor(context.getColor(R.color.softred));
            } else if (!"".equals(getItem(position))){
                tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.white));
                tvItemGridView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
        }

        dbHandler = DBHandler.open(context);
        ImageDataBean imageDataBean = new ImageDataBean();
        if (!list.get(position).equals("")) {
            Log.i(TAG, position  + "=" + list.get(position));
            imageDataBean = dbHandler.selectDate(getItem(position));
        }
        dbHandler.close();
        try {
            if (imageDataBean.getImage().length > 0) {
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageDataBean.getImage(), 0, imageDataBean.getImage().length));
            } else {
                imageView.setImageBitmap(null);
            }
        } catch (Exception e) {
            Log.i(TAG, "image load error ... ") ;
        }

        tv1.setFocusable(true);

        return convertView;
    }
}
