package com.billcoreatech.calendarexam411;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.billcoreatech.calendarexam411.database.DBHandler;
import com.billcoreatech.calendarexam411.databinding.ActivityCalendarViewBinding;
import com.billcoreatech.calendarexam411.databinding.PopupWindowBinding;
import com.billcoreatech.calendarexam411.util.GridAdapter;
import com.billcoreatech.calendarexam411.util.ImageDataBean;
import com.billcoreatech.calendarexam411.util.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarView extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 100 ;
    private static final int PICK_FROM_CAMERA = 101 ;
    private static final int CROP_FROM_IMAGE = 102 ;
    private static final int GET_GALLERY_IMAGE = 200 ;
    String TAG = "CalendarView" ;
    ActivityCalendarViewBinding binding ;
    PopupWindowBinding popupBinding ;
    GridAdapter gridAdapter;
    ArrayList<String> dayList;
    SimpleDateFormat curYearFormat ;
    SimpleDateFormat curMonthFormat ;
    private GestureDetectorCompat detector;
    Date pDate ;
    DBHandler dbHandler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        curYearFormat = new SimpleDateFormat("yyyy") ;
        curMonthFormat = new SimpleDateFormat("MM") ;
        detector = new GestureDetectorCompat(this, new MyGestureListener());

        long now = System.currentTimeMillis() ;
        pDate = new Date(now);

        getDispMonth(pDate);

        binding.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "click..." + dayList.get(position));
                try {
                    pDate = StringUtil.getStringDate(dayList.get(position));
                    binding.textDate.setText(StringUtil.getDispDayYMD(dayList.get(position)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarView.this);
                builder.setTitle("삭제")
                        .setMessage("등록할 이미지를 삭제할까요 ???")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHandler = DBHandler.open(CalendarView.this);
                                dbHandler.deleteOne(dayList.get(position));
                                dbHandler.close();
                                Toast.makeText(getApplicationContext(), "삭제 했습니다.", Toast.LENGTH_LONG).show();
                                getDispMonth(pDate);
                            }
                        })
                        .setNegativeButton("닫기", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        binding.gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });

        binding.txtYearMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onButtonShowPopupWindowClick(v, pDate);
            }
        });

        binding.btnLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case GET_GALLERY_IMAGE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    Uri photoUri = data.getData();
                    cropImage(photoUri);
                }
                break;
            case PICK_FROM_CAMERA:
                break;
            case CROP_FROM_IMAGE:
                Bundle bundle = data.getExtras();
                assert bundle != null;
                Bitmap test = bundle.getParcelable("data");
                binding.imageView2.setImageBitmap(test);

                dbHandler = DBHandler.open(CalendarView.this);
                long lId = dbHandler.insertDayinfo(StringUtil.getDateString(pDate), "", dbHandler.getByteArrayFromBitmap(test));
                if (lId > -1) {
                    Toast.makeText(getApplicationContext(), "저장이 되었습니다.", Toast.LENGTH_LONG).show();
                }
                dbHandler.close();
                getDispMonth(pDate);
                break;
        }
    }

    private void cropImage(Uri imageUri) {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

/*        Intent intent= getCropIntent(imageUri);
        startActivityForResult(intent, CROP_FROM_IMAGE);*/
    }

    private Intent getCropIntent(Uri inputUri) {
        Intent intent = new                      Intent("com.android.camera.action.CROP");
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.setDataAndType(inputUri, "image/*");

        intent.putExtra("aspectX", 4);
        intent.putExtra("aspectY", 3);
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);

        //intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("return-data", true);

        return intent;
    }


    private void getDispMonth(Date pDate) {

        binding.txtYearMonth.setText(curYearFormat.format(pDate) + "." + curMonthFormat.format(pDate));
        dayList = new ArrayList<String>();
        Calendar mCal = Calendar.getInstance();
        //이번달 1일 무슨요일인지 판단 mCal.set(Year,Month,Day)
        mCal.set(Integer.parseInt(curYearFormat.format(pDate)), Integer.parseInt(curMonthFormat.format(pDate)) - 1, 1);
        int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
        //1일 - 요일 매칭 시키기 위해 공백 add
        for (int i = 1; i < dayNum; i++) {
            dayList.add("");
        }
        setCalendarDate(mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH) + 1);

        gridAdapter = new GridAdapter(getApplicationContext(), dayList);
        gridAdapter.updateReceiptsList(dayList);
        binding.gridView.setAdapter(gridAdapter);

    }

    private void setCalendarDate(int year, int month) {
        Calendar mCal = Calendar.getInstance();
        mCal.set(Calendar.YEAR, year) ;
        mCal.set(Calendar.MONTH, month - 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd") ;
        int iNext = 0 ;
        for (int i = 0; i < mCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            mCal.set(Calendar.DAY_OF_MONTH, i + 1);
            dayList.add(sdf.format(new Date(mCal.getTimeInMillis())));
            iNext = mCal.get(Calendar.DAY_OF_WEEK) ;
            Log.d(TAG,"week :" + mCal.get(Calendar.DAY_OF_WEEK)) ;
        }
        // 나머지 빈칸도 채우기 위해서
        for (int i = iNext ; i < 7 ; i++) {
            dayList.add("") ;
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            float diffY = event2.getY() - event1.getY();
            float diffX = event2.getX() - event1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
            }
            return true;
        }
    }

    private void onSwipeLeft() {
        pDate = StringUtil.addMonth(pDate, 1) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    private void onSwipeRight() {
        pDate = StringUtil.addMonth(pDate, -1) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    private void onSwipeTop() {
        pDate = StringUtil.addMonth(pDate, 12) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    private void onSwipeBottom() {
        pDate = StringUtil.addMonth(pDate, -12) ;
        getDispMonth(pDate);
        binding.gridView.setFocusable(true);
    }

    public void onButtonShowPopupWindowClick(View view, Date ppDate) {

        popupBinding = PopupWindowBinding.inflate(getLayoutInflater()) ;
        View popupView = popupBinding.getRoot() ;

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 300, 600);

        popupBinding.txtMonth.setText(curYearFormat.format(pDate)) ;
        Calendar mCal = Calendar.getInstance();
        mCal.set(Integer.parseInt(curYearFormat.format(pDate)), Integer.parseInt(curMonthFormat.format(pDate)) - 1, 1);
        switch(mCal.get(Calendar.MONTH)) {
            case 0: onSetColor(popupBinding.txtMonth1) ;
                break ;
            case 1: onSetColor(popupBinding.txtMonth2) ;
                break ;
            case 2: onSetColor(popupBinding.txtMonth3) ;
                break ;
            case 3: onSetColor(popupBinding.txtMonth4) ;
                break ;
            case 4: onSetColor(popupBinding.txtMonth5) ;
                break ;
            case 5: onSetColor(popupBinding.txtMonth6) ;
                break ;
            case 6: onSetColor(popupBinding.txtMonth7) ;
                break ;
            case 7: onSetColor(popupBinding.txtMonth8) ;
                break ;
            case 8: onSetColor(popupBinding.txtMonth9) ;
                break ;
            case 9: onSetColor(popupBinding.txtMonth10) ;
                break ;
            case 10: onSetColor(popupBinding.txtMonth11) ;
                break ;
            case 11: onSetColor(popupBinding.txtMonth12) ;
                break ;
        }
        popupBinding.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.addMonth(pDate, -12) ;
                popupBinding.txtMonth.setText(curYearFormat.format(pDate)) ;
            }
        });
        popupBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.addMonth(pDate, 12) ;
                popupBinding.txtMonth.setText(curYearFormat.format(pDate)) ;
            }
        });
        popupBinding.txtMonth1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 1);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 2);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 3);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 4);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 5);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 6);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 7);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 8);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 9);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 10);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 11);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.txtMonth12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDate = StringUtil.getDay(pDate, 12);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
        popupBinding.btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                pDate = new Date(now);
                popupWindow.dismiss();
                getDispMonth(pDate);
            }
        });
    }

    private void onSetColor(TextView txtMonth) {
        txtMonth.setBackgroundColor(getColor(R.color.softblue));
        txtMonth.setTextColor(Color.YELLOW) ;
    }
}