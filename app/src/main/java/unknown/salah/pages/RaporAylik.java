package unknown.salah.pages;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unknown.salah.DBHelper.DBHelper;
import unknown.salah.R;
import unknown.salah.Util.TimeUtil;

public class RaporAylik extends Fragment {

    protected PieChart chart;
    private TextView raporHeadr;
    private String month;
    private TextView noDataMonthly;

    private DBHelper db;
    private TimeUtil tUtil;

    private List<Integer> newnamazVakits;
    private Map<Integer, Integer> newnamazMap;

    public static final String NAMAZ_COLUMN_VAKIT = "namaz_vakit";
    public static final String NAMAZ_COLUMN_VAKIT_DEGER = "namaz_vakit_deger";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_raporlar_aylik, container, false);

        db = new DBHelper(getContext());
        tUtil = new TimeUtil();

        Map<Integer, Integer> namazMap = new HashMap<Integer, Integer>();
        DateFormat dayt = new SimpleDateFormat("yyyyMMdd");
        month = dayt.format(Calendar.getInstance().getTime());

        namazMap = getMonthlyData(month);

        List<Integer> namazVakits = new ArrayList<Integer>();
        Set<Integer> keys = namazMap.keySet();
        for (Integer i : keys) {
            namazVakits.add(i);
        }

        Collections.sort(namazVakits);

        chart = (PieChart) rootView.findViewById(R.id.raporMonthChart);
        noDataMonthly = (TextView) rootView.findViewById((R.id.noDataMonthly));

        raporHeadr = (TextView) rootView.findViewById(R.id.raporMonth_date_txt);

        ImageView dateLeft = (ImageView) rootView.findViewById(R.id.raporMonth_date_left);
        ImageView dateRight = (ImageView) rootView.findViewById(R.id.raporMonth_date_right);

        dateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dayt = new SimpleDateFormat("yyyyMMdd");
                try {
                    Date date = dayt.parse(month);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.add(Calendar.MONTH, -1);
                    month = dayt.format(cal.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                raporHeadr.setText(tUtil.convertDateMonthFormat(month));

                newnamazMap = new HashMap<Integer, Integer>();
                newnamazMap = getMonthlyData(month);

                newnamazVakits = new ArrayList<Integer>();
                Set<Integer> keys = newnamazMap.keySet();
                for (Integer i : keys) {
                    newnamazVakits.add(i);
                }

                Collections.sort(newnamazVakits);

                if(newnamazMap.size() > 0) {
                    noDataMonthly.setVisibility(View.GONE);
                    chart.setVisibility(View.VISIBLE);

                    chart.invalidate();
                    chart.getData().notifyDataChanged();
                    chart.notifyDataSetChanged();
                    chartConfigure();
                    setData(newnamazMap, newnamazVakits);
                }
                else{
                    noDataMonthly.setVisibility(View.VISIBLE);
                    chart.setVisibility(View.GONE);
                }
            }
        });

        dateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dayt = new SimpleDateFormat("yyyyMMdd");
                String today = dayt.format(Calendar.getInstance().getTime());
                if (!month.equalsIgnoreCase(today)) {
                    try {
                        Date date = dayt.parse(month);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        cal.add(Calendar.MONTH, +1);
                        month = dayt.format(cal.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    raporHeadr.setText(tUtil.convertDateMonthFormat(month));

                    newnamazMap = new HashMap<Integer, Integer>();
                    newnamazMap = getMonthlyData(month);

                    newnamazVakits = new ArrayList<Integer>();
                    Set<Integer> keys = newnamazMap.keySet();
                    for (Integer i : keys) {
                        newnamazVakits.add(i);
                    }

                    Collections.sort(newnamazVakits);
                    if(newnamazMap.size() > 0) {
                        noDataMonthly.setVisibility(View.GONE);
                        chart.setVisibility(View.VISIBLE);

                        chart.invalidate();
                        chart.getData().notifyDataChanged();
                        chart.notifyDataSetChanged();
                        chartConfigure();
                        setData(newnamazMap, newnamazVakits);
                    }
                    else{
                        noDataMonthly.setVisibility(View.VISIBLE);
                        chart.setVisibility(View.GONE);
                    }
                }
            }
        });

        raporHeadr.setText(tUtil.convertDateMonthFormat(month));

        if(namazMap.size() > 0){
            noDataMonthly.setVisibility(View.GONE);
            chart.setVisibility(View.VISIBLE);

            chart.invalidate();
            chartConfigure();
            setData(namazMap,namazVakits);
        }
        else{
            noDataMonthly.setVisibility(View.VISIBLE);
            chart.setVisibility(View.GONE);
        }
        return rootView;
    }

    private Map<Integer,Integer> getMonthlyData(String month) {
        Map<Integer, Integer> namazMap = new HashMap<Integer, Integer>();

        //Query with first day of month
        month = month.substring(0,6)+"01";

        //Next Month :)
        String nextMonth = String.valueOf(Integer.valueOf(month.substring(0,6))+1)+"01";

        Cursor cursor = db.getNamazInfoMonthly(Integer.parseInt(month),Integer.parseInt(nextMonth));

        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                if (cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT_DEGER)) != 0) {
                    if (namazMap.containsKey(cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT)))) {
                        namazMap.put(cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT)), namazMap.get(cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT))) + 1);
                    } else {
                        namazMap.put(cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT)), 1);
                    }
                }
                cursor.moveToNext();
            }
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return namazMap;

    }

    private void chartConfigure(){
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setCenterText(getResources().getString(R.string.app_name));

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);

        chart.setRotationAngle(0);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        chart.setEntryLabelColor(Color.DKGRAY);
        chart.setEntryLabelTextSize(12f);
    }

    private void setData(Map<Integer, Integer> namazMap, List<Integer> namazVakits) {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        String nmzVkt="";
        String[] nmzVktArray = getResources().getStringArray(R.array.namaz_vakitleri_text);
        for (int i = 0; i < namazVakits.size() ; i++) {
            if(namazVakits.get(i)== i + 1){
                nmzVkt = nmzVktArray[i];
            }
            entries.add(new PieEntry((float)namazMap.get(namazVakits.get(i)),
                    nmzVkt));
        }

        PieDataSet dataSet = new PieDataSet(entries, getResources().getString(R.string.app_name));

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.GRAY);
        chart.setData(data);
        chart.highlightValues(null);
    }
}