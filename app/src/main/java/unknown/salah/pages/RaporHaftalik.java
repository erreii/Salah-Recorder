package unknown.salah.pages;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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
import unknown.salah.MainActivity;
import unknown.salah.Model.RaporWeek;
import unknown.salah.R;
import unknown.salah.Util.TimeUtil;

public class RaporHaftalik extends Fragment {

    private DBHelper db;
    private TextView raporHeadr;

    protected SampleAdapter mAdapter;
    private String dateTo;
    private String dateFrom;

    private List<Integer> namazDates;

    private List<RaporWeek> rprWeek;
    private TimeUtil timeUtil;

    Fragment fragment = null;

    private boolean isFirstTime = true;
    private List<String> nmzLst;
    private Map<Integer,List<String>> eachDateVal;
    private ListView listView;
    private LinearLayout bottom_panel_weekly;
    private TextView noDataWeekly;
    private TableRow rowheaderWeekly;
    private TextView percentSabah;
    private TextView percentOgle;
    private TextView percentIkindi;
    private TextView percentAksam;
    private TextView percentYatsi;

    private static final int NAMAZ_GEC_KILDI = 1;
    private static final int NAMAZ_KILDI = 2;
    private static final int NAMAZ_CAMIDE_KILDI = 3;

    private static final int ZERO = 0;

    private static final int NAMAZ_SABAH = 1;
    private static final int NAMAZ_OGLE = 2;
    private static final int NAMAZ_IKINDI = 3;
    private static final int NAMAZ_AKSAM = 4;
    private static final int NAMAZ_YATSI = 5;

    public static final String NAMAZ_COLUMN_TARIH = "namaz_tarih";
    public static final String NAMAZ_COLUMN_VAKIT = "namaz_vakit";
    public static final String NAMAZ_COLUMN_VAKIT_DEGER = "namaz_vakit_deger";

    private static final String SELECTED_DATE = "selectedDate";
    private static final String SELECTED_DATE_VALUE = "selectedDate_value";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_raporlar_haftalik, container, false);

        db = new DBHelper(getContext());
        timeUtil = new TimeUtil();
        nmzLst = new ArrayList<String>();
        eachDateVal = new HashMap<Integer,List<String>>();

        rprWeek = new ArrayList<>();
        getDates(isFirstTime);
        nmzLst = getWeekData(dateFrom, dateTo);

        getDataForListView(nmzLst,eachDateVal);

        listView = (ListView) rootView.findViewById(R.id.listRaporWeek);
        mAdapter = new SampleAdapter();
        listView.setAdapter(mAdapter);


        ImageView dateLeft = (ImageView) rootView.findViewById(R.id.raporWeek_date_left);
        ImageView dateRight = (ImageView) rootView.findViewById(R.id.raporWeek_date_right);

        raporHeadr = (TextView) rootView.findViewById(R.id.raporWeek_date_txt);
        noDataWeekly = (TextView) rootView.findViewById(R.id.noDataWeekly);
        bottom_panel_weekly = (LinearLayout)rootView.findViewById(R.id.bottom_panel_weekly);
        rowheaderWeekly = (TableRow) rootView.findViewById(R.id.rowheaderWeekly);

        percentSabah = (TextView) rootView.findViewById(R.id.percentSabah);
        percentOgle = (TextView) rootView.findViewById(R.id.percentOgle);
        percentIkindi = (TextView) rootView.findViewById(R.id.percentIkindi);
        percentAksam = (TextView) rootView.findViewById(R.id.percentAksam);
        percentYatsi = (TextView) rootView.findViewById(R.id.percentYatsi);

        if(nmzLst.size() > ZERO){
            calcPercent(nmzLst);
        }
        else{
            noDataWeekly.setVisibility(View.VISIBLE);
        }

        dateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDates(!isFirstTime);
                raporHeadr.setText(timeUtil.convertDateFormat(dateFrom) + " / " + timeUtil.convertDateFormat(dateTo));
                nmzLst = new ArrayList<String>();
                rprWeek = new ArrayList<>();
                nmzLst = getWeekData(dateFrom, dateTo);
                getDataForListView(nmzLst,eachDateVal);
                if(nmzLst.size() == 0){
                    noDataWeekly.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    bottom_panel_weekly.setVisibility(View.GONE);
                    rowheaderWeekly.setVisibility(View.GONE);
                }
                else{
                    listView.setVisibility(View.VISIBLE);
                    noDataWeekly.setVisibility(View.GONE);
                    calcPercent(nmzLst);
                }
                listView.invalidate();
                mAdapter.notifyDataSetChanged();
            }
        });


        dateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dayt = new SimpleDateFormat("yyyyMMdd");
                String today = dayt.format(Calendar.getInstance().getTime());
                if (!dateTo.equalsIgnoreCase(today)) {
                    DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

                    try {
                        Date date = formatter.parse(dateTo);
                        Calendar cal2 = Calendar.getInstance();
                        cal2.setTime(date);
                        cal2.add(Calendar.DATE, +6);
                        dateFrom = dateTo;
                        dateTo = formatter.format(cal2.getTime());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    raporHeadr.setText(timeUtil.convertDateFormat(dateFrom) + " / " + timeUtil.convertDateFormat(dateTo));
                    nmzLst = new ArrayList<String>();
                    rprWeek = new ArrayList<>();
                    nmzLst = getWeekData(dateFrom, dateTo);
                    getDataForListView(nmzLst,eachDateVal);
                    if(nmzLst.size() == 0){
                        noDataWeekly.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        bottom_panel_weekly.setVisibility(View.GONE);
                        rowheaderWeekly.setVisibility(View.GONE);
                    }
                    else{
                        listView.setVisibility(View.VISIBLE);
                        noDataWeekly.setVisibility(View.GONE);
                        calcPercent(nmzLst);
                    }
                    listView.invalidate();
                    mAdapter.notifyDataSetChanged();
                }
                else{
                    Toast.makeText(getContext(), getResources().getString(R.string.datepicker_ileri_tarih), Toast.LENGTH_SHORT).show();
                }
            }
        });
        raporHeadr.setText(timeUtil.convertDateFormat(dateFrom) + " / " + timeUtil.convertDateFormat(dateTo));

        return rootView;
    }

    private void calcPercent(List<String> nmzLst){
        int sabah = ZERO;
        int ogle = ZERO;
        int aksam = ZERO;
        int ikindi = ZERO;
        int yatsi = ZERO;

        for(String str:nmzLst){
            String nmzVakt = str.split("-")[1];
            String nmzDegr = str.split("-")[2];

            if(String.valueOf(NAMAZ_SABAH).equals(nmzVakt) && !String.valueOf(ZERO).equals(nmzDegr)){
                sabah++;
            }
            else if(String.valueOf(NAMAZ_OGLE).equals(nmzVakt) && !String.valueOf(ZERO).equals(nmzDegr)){
                ogle++;
            }
            else if(String.valueOf(NAMAZ_IKINDI).equals(nmzVakt) && !String.valueOf(ZERO).equals(nmzDegr)){
                ikindi++;
            }
            else if(String.valueOf(NAMAZ_AKSAM).equals(nmzVakt) && !String.valueOf(ZERO).equals(nmzDegr)){
                aksam++;
            }
            else if(String.valueOf(NAMAZ_YATSI).equals(nmzVakt) && !String.valueOf(ZERO).equals(nmzDegr)){
                yatsi++;
            }
        }


        bottom_panel_weekly.setVisibility(View.VISIBLE);
        rowheaderWeekly.setVisibility(View.VISIBLE);

        percentSabah.setText(String.valueOf(sabah *100/7)+"%");
        percentOgle.setText(String.valueOf(ogle *100/7)+"%");
        percentAksam.setText(String.valueOf(aksam *100/7)+"%");
        percentIkindi.setText(String.valueOf(ikindi *100/7)+"%");
        percentYatsi.setText(String.valueOf(yatsi *100/7)+"%");
    }

    private void getDataForListView(List<String> nmzLst,Map<Integer,List<String>> eachDateVal){
        namazDates = new ArrayList<Integer>();
        for (int i=0; i< nmzLst.size();i++){
            int ndate = Integer.valueOf(nmzLst.get(i).split("-")[0]);
            if(!namazDates.contains(ndate)) {
                namazDates.add(ndate);
            }

            int ntime = Integer.valueOf(nmzLst.get(i).split("-")[1]);
            int nvalue = Integer.valueOf(nmzLst.get(i).split("-")[2]);

            List<String> dayValues = new ArrayList<String>();
            dayValues.add(ntime + "-" + nvalue);

            if(eachDateVal.containsKey(ndate)){
                eachDateVal.get(ndate).add(ntime + "-" + nvalue);
            }
            else{
                eachDateVal.put(ndate,dayValues);
            }
        }

        for(int j=0;j<namazDates.size();j++){
            rprWeek.add(new RaporWeek(namazDates.get(j),eachDateVal));
        }
    }

    private void getDates(boolean isFirstTime) {
        if (isFirstTime == true) {
            DateFormat dayt = new SimpleDateFormat("yyyyMMdd");
            dateTo = dayt.format(Calendar.getInstance().getTime());

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -6);
            dateFrom = dayt.format(cal.getTime());
        } else {
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

            try {
                Date date = formatter.parse(dateFrom);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(date);
                cal2.add(Calendar.DATE, -6);
                dateTo = dateFrom;
                dateFrom = formatter.format(cal2.getTime());

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getWeekData(String beforeDate, String anaMenuDate) {

        List<String> namazLst = new ArrayList<String>();

        Cursor cursor = db.getNamazInfoWeekly(Integer.parseInt(beforeDate), Integer.parseInt(anaMenuDate));

        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                    StringBuilder str = new StringBuilder();
                    str = str.append(cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_TARIH))).append("-" + cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT))).append("-" + cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT_DEGER)));

                    if (!namazLst.contains(str)) {
                        namazLst.add(str.toString());
                    }

                cursor.moveToNext();
            }
        }
        if (!cursor.isClosed())
        {
            cursor.close();
        }

        return namazLst;
    }

    private class SampleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return rprWeek.size();
        }

        @Override
        public RaporWeek getItem(int position) {
            return rprWeek.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_rapor_week, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final RaporWeek item = getItem(position);

            holder.raporTrh.setText(timeUtil.convertDated(String.valueOf(item.getTarih())));

            List<String> items = new ArrayList<>();
            items = item.getValues().get(item.getTarih());
            String nmzVkt= "";
            String nmzVlue ="";

            for(int i=0;i<items.size();i++){
                nmzVkt = items.get(i).split("-")[0];
                nmzVlue = items.get(i).split("-")[1];
                if(nmzVkt.equals(String.valueOf(NAMAZ_SABAH))){
                    if(nmzVlue.equals(String.valueOf(NAMAZ_GEC_KILDI))){
                        holder.rpr_dt_sbh.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_KILDI))){
                        holder.rpr_dt_sbh.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_CAMIDE_KILDI))){
                        holder.rpr_dt_sbh.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                    }
                    else{
                        holder.rpr_dt_sbh.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    }
                }
                else if(nmzVkt.equals(String.valueOf(NAMAZ_OGLE))){
                    if(nmzVlue.equals(String.valueOf(NAMAZ_GEC_KILDI))){
                        holder.rpr_dt_ogle.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_KILDI))){
                        holder.rpr_dt_ogle.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_CAMIDE_KILDI))){
                        holder.rpr_dt_ogle.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                    }
                    else{
                        holder.rpr_dt_ogle.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    }
                }
                else if(nmzVkt.equals(String.valueOf(NAMAZ_IKINDI))){
                    if(nmzVlue.equals(String.valueOf(NAMAZ_GEC_KILDI))){
                        holder.rpr_dt_ikindi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_KILDI))){
                        holder.rpr_dt_ikindi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_CAMIDE_KILDI))){
                        holder.rpr_dt_ikindi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                    }
                    else{
                        holder.rpr_dt_ikindi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    }
                }
                else if(nmzVkt.equals(String.valueOf(NAMAZ_AKSAM))){
                    if(nmzVlue.equals(String.valueOf(NAMAZ_GEC_KILDI))){
                        holder.rpr_dt_aksam.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_KILDI))){
                        holder.rpr_dt_aksam.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_CAMIDE_KILDI))){
                        holder.rpr_dt_aksam.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                    }
                    else{
                        holder.rpr_dt_aksam.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    }
                }
                else if(nmzVkt.equals(String.valueOf(NAMAZ_YATSI))){
                    if(nmzVlue.equals(String.valueOf(NAMAZ_GEC_KILDI))){
                        holder.rpr_dt_yatsi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_KILDI))){
                        holder.rpr_dt_yatsi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                    }
                    else if(nmzVlue.equals(String.valueOf(NAMAZ_CAMIDE_KILDI))){
                        holder.rpr_dt_yatsi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                    }
                    else{
                        holder.rpr_dt_yatsi.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    }
                }
            }

            holder.list_rapor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weekReports(item.getTarih());
                }
            });

            holder.raporTrh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weekReports(item.getTarih());
                }
            });

            holder.rpr_dt_sbh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weekReports(item.getTarih());
                }
            });

            holder.rpr_dt_ogle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weekReports(item.getTarih());
                }
            });

            holder.rpr_dt_ikindi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weekReports(item.getTarih());
                }
            });

            holder.rpr_dt_aksam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weekReports(item.getTarih());
                }
            });

            holder.rpr_dt_yatsi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weekReports(item.getTarih());
                }
            });

            return convertView;
        }

        public void weekReports(int date){
            Bundle dataBundle = new Bundle();
            dataBundle.putBoolean(SELECTED_DATE, true);
            dataBundle.putString(SELECTED_DATE_VALUE, String.valueOf(date));

            fragment = new Namaz();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            fragment.setArguments(dataBundle);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

    }

    private static class ViewHolder {

        private View view;

        private LinearLayout list_rapor;
        private TextView raporTrh;
        private ImageView rpr_dt_sbh;
        private ImageView rpr_dt_ogle;
        private ImageView rpr_dt_ikindi;
        private ImageView rpr_dt_aksam;
        private ImageView rpr_dt_yatsi;

        public ViewHolder(View view) {
            this.view = view;
            list_rapor = (LinearLayout) view.findViewById(R.id.list_rapor);
            raporTrh = (TextView) view.findViewById(R.id.raporTrh);
            rpr_dt_sbh = (ImageView) view.findViewById(R.id.rpr_dt_sbh);
            rpr_dt_ogle = (ImageView) view.findViewById(R.id.rpr_dt_ogle);
            rpr_dt_ikindi = (ImageView) view.findViewById(R.id.rpr_dt_ikindi);
            rpr_dt_aksam = (ImageView) view.findViewById(R.id.rpr_dt_aksam);
            rpr_dt_yatsi = (ImageView) view.findViewById(R.id.rpr_dt_yatsi);
        }
    }

}
