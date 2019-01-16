package unknown.salah.pages;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import unknown.salah.DBHelper.DBHelper;
import unknown.salah.Model.NamazModel;
import unknown.salah.R;
import unknown.salah.Util.TimeUtil;

public class Namaz extends Fragment implements CalendarDatePickerDialogFragment.OnDateSetListener {
    private int screenWidth;

    private static final int NAMAZ_NOT_SELECTED = 0;
    private static final int NAMAZ_GEC_KILDI = 1;
    private static final int NAMAZ_KILDI = 2;
    private static final int NAMAZ_CAMIDE_KILDI = 3;

    private static final int NAMAZ_SABAH = 1;
    private static final int NAMAZ_OGLE = 2;
    private static final int NAMAZ_IKINDI = 3;
    private static final int NAMAZ_AKSAM = 4;
    private static final int NAMAZ_YATSI = 5;

    private List<NamazModel> namazlist;
    protected SampleAdapter mAdapter;
    Fragment fragment = null;

    private DBHelper db;
    private TimeUtil time;
    private String anaMenuDate;
    private long currentInMillis;

    private ImageView sunsetForImg;
    private TextView namaz_gun_sayi_txt;
    private TextView namaz_tarih_txt;
    private TextView namaz_gun_metin_txt;

    private static final String NAMAZ_DATE_PICKER = "namaz_date_picker";

    public static final String NAMAZ_COLUMN_TARIH = "namaz_tarih";
    public static final String NAMAZ_COLUMN_VAKIT = "namaz_vakit";
    public static final String NAMAZ_COLUMN_VAKIT_DEGER = "namaz_vakit_deger";

    private static final String SELECTED_DATE = "selectedDate";
    private static final String SELECTED_DATE_VALUE = "selectedDate_value";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_namaz, container, false);

        db = new DBHelper(container.getContext());
        time = new TimeUtil();

        sunsetForImg = (ImageView) rootView.findViewById(R.id.sunsetForImg);
        namaz_gun_sayi_txt = (TextView) rootView.findViewById(R.id.namaz_gun_sayi_txt);
        namaz_tarih_txt = (TextView) rootView.findViewById(R.id.namaz_tarih_txt);
        namaz_gun_metin_txt = (TextView) rootView.findViewById(R.id.namaz_gun_metin_txt);

        if (getArguments().getBoolean(SELECTED_DATE)) {
            anaMenuDate = getArguments().getString(SELECTED_DATE_VALUE);
        } else {
            anaMenuDate = time.getCurrentDate();
        }

        currentInMillis = time.getInMillis(time.getCurrentDate());

        try {
            setAnaMenuDate(anaMenuDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        if (getNamazInfos()) {
            insertNotSelectDB(namazlist, Integer.valueOf(anaMenuDate));
        }

        sunsetForImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setDoneText(getResources().getString(R.string.datepicker_tamam))
                        .setCancelText(getResources().getString(R.string.datepicker_iptal))
                        .setOnDateSetListener(Namaz.this);
                cdp.show(getFragmentManager(), NAMAZ_DATE_PICKER);
            }
        });

        namaz_gun_sayi_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setDoneText(getResources().getString(R.string.datepicker_tamam))
                        .setCancelText(getResources().getString(R.string.datepicker_iptal))
                        .setOnDateSetListener(Namaz.this);
                cdp.show(getFragmentManager(), NAMAZ_DATE_PICKER);
            }
        });

        namaz_gun_metin_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setDoneText(getResources().getString(R.string.datepicker_tamam))
                        .setCancelText(getResources().getString(R.string.datepicker_iptal))
                        .setOnDateSetListener(Namaz.this);
                cdp.show(getFragmentManager(), NAMAZ_DATE_PICKER);
            }
        });

        namaz_tarih_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setDoneText(getResources().getString(R.string.datepicker_tamam))
                        .setCancelText(getResources().getString(R.string.datepicker_iptal))
                        .setOnDateSetListener(Namaz.this);
                cdp.show(getFragmentManager(), NAMAZ_DATE_PICKER);
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.listNamazTimes);
        mAdapter = new SampleAdapter();
        listView.setAdapter(mAdapter);

        YoYo.with(Techniques.Flash).duration(1000).playOn(namaz_gun_sayi_txt);

        return rootView;
    }

    private boolean getNamazInfos() {
        Cursor cursor = db.getNamazInfoWithDate(Integer.valueOf(anaMenuDate));
        cursor.moveToFirst();
        boolean isFirstTime = false;
        namazlist = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (!cursor.isAfterLast()) {
                if (cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_TARIH)) == Integer.valueOf(anaMenuDate)) {
                    namazlist.add(new NamazModel(Integer.valueOf(anaMenuDate), cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT)), cursor.getInt(cursor.getColumnIndex(NAMAZ_COLUMN_VAKIT_DEGER))));
                }
                cursor.moveToNext();
            }
        } else {
            int[] namaz_vakitleri = getResources().getIntArray(R.array.namaz_vakitleri);
            for (int namaz_vakit : namaz_vakitleri) {
                namazlist.add(new NamazModel(Integer.valueOf(anaMenuDate), namaz_vakit, NAMAZ_NOT_SELECTED));
            }
            isFirstTime = true;
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return isFirstTime;
    }

    //Sayfayi ilk actiginda veya tarih sectiginde 0 insert et
    private void insertNotSelectDB(List<NamazModel> namazlist, Integer namazTarih) {
        Cursor cursor = db.noOfDayNamaz(Integer.valueOf(anaMenuDate));
        cursor.moveToFirst();
        if (cursor.getCount() <= 0) {
            for (int i = 0; i < namazlist.size(); i++) {
                db.insertNamaz(namazTarih, namazlist.get(i).getNamazVakit(), namazlist.get(i).getNerdeKildi());
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {

        @SuppressLint("StringFormatMatches") String datePick = getString(R.string.calendar_date_picker_result_values, year, monthOfYear + 1, dayOfMonth);
        if (monthOfYear + 1 < 10) {
            StringBuilder str = new StringBuilder();
            str = str.append(year).append("-0"+ (monthOfYear+1)).append("-"+dayOfMonth);
            datePick = str.toString();
        }

        if(dayOfMonth < 10){
            StringBuilder str2 = new StringBuilder();
            str2 = str2.append(datePick.split("-")[0] + (datePick.split("-")[1]).split("-")[0]).append("0"+ dayOfMonth);
            datePick = str2.toString();
        }
        datePick = datePick.replaceAll("-", "");
        long pickedInMillis = time.getInMillis(datePick);

        if (pickedInMillis > currentInMillis) {
            Toast.makeText(getContext(), getResources().getString(R.string.datepicker_ileri_tarih), Toast.LENGTH_SHORT).show();
        } else {
            Bundle dataBundle = new Bundle();
            dataBundle.putBoolean(SELECTED_DATE, true);
            dataBundle.putString(SELECTED_DATE_VALUE, datePick);

            fragment = new Namaz();
            FragmentManager fragmentManager = getFragmentManager();
            fragment.setArguments(dataBundle);
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        }
    }

    private class SampleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return namazlist.size();
        }

        @Override
        public NamazModel getItem(int position) {
            return namazlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.listnamazitem, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final NamazModel item = getItem(position);

            setColors(position, convertView, holder);
            String[] namazVakit_text = getResources().getStringArray(R.array.namaz_vakitleri_text);

            if (1 == item.getNamazVakit()) {
                holder.namazVakti.setText(namazVakit_text[0]);
            } else if (2 == item.getNamazVakit()) {
                holder.namazVakti.setText(namazVakit_text[1]);
            } else if (3 == item.getNamazVakit()) {
                holder.namazVakti.setText(namazVakit_text[2]);
            } else if (4 == item.getNamazVakit()) {
                holder.namazVakti.setText(namazVakit_text[3]);
            } else if (5 == item.getNamazVakit()) {
                holder.namazVakti.setText(namazVakit_text[4]);
            }

            if (NAMAZ_NOT_SELECTED == item.getNerdeKildi()) {
                holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                holder.select_state_txt.setText(R.string.not_prayed);
            } else if (NAMAZ_GEC_KILDI == item.getNerdeKildi()) {
                holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                holder.select_state_txt.setText(R.string.late_prayed);
            } else if (NAMAZ_KILDI == item.getNerdeKildi()) {
                holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                holder.select_state_txt.setText(R.string.prayed);
            } else if (NAMAZ_CAMIDE_KILDI == item.getNerdeKildi()) {
                holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                holder.select_state_txt.setText(R.string.mosque_prayed);
            }

            holder.nerdeKildi_layout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            holder.selection.setVisibility(View.GONE);
            holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));

            holder.namazVakti.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.nerdeKildi_layout.getVisibility() == View.GONE) {
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.selection.setVisibility(View.GONE);
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                    } else {
                        holder.nerdeKildi_layout.setVisibility(View.GONE);
                        holder.selection.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(5).playOn(holder.namaz_selection);
                        YoYo.with(Techniques.SlideInRight).duration(5).playOn(holder.selection);
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.selection.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        YoYo.with(Techniques.Landing).duration(500).playOn(holder.namazVakti);
                    }
                }
            });

            holder.nerdeKildi_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.nerdeKildi_layout.getVisibility() == View.GONE) {
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.selection.setVisibility(View.GONE);
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                    } else {
                        holder.nerdeKildi_layout.setVisibility(View.GONE);
                        holder.selection.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(5).playOn(holder.namaz_selection);
                        YoYo.with(Techniques.SlideInRight).duration(5).playOn(holder.selection);
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.selection.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        YoYo.with(Techniques.Landing).duration(500).playOn(holder.namazVakti);
                    }
                }
            });

            holder.select_gecKildi_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NAMAZ_GEC_KILDI == item.getNerdeKildi()) {
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        item.setNerdeKildi(NAMAZ_NOT_SELECTED);
                        holder.select_state_txt.setText(R.string.not_prayed);
                        holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                        holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                        holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    } else {
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        item.setNerdeKildi(NAMAZ_GEC_KILDI);
                        holder.select_state_txt.setText(R.string.late_prayed);
                        holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                        holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                        holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                    }
                    updateNamazInfo(position, item.getNerdeKildi());
                }
            });

            holder.select_kildi_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NAMAZ_KILDI == item.getNerdeKildi()) {
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        item.setNerdeKildi(NAMAZ_NOT_SELECTED);
                        holder.select_state_txt.setText(R.string.not_prayed);
                        holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                        holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                        holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    } else {
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        item.setNerdeKildi(NAMAZ_KILDI);
                        holder.select_state_txt.setText(R.string.prayed);
                        holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                        holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                        holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                    }
                    updateNamazInfo(position, item.getNerdeKildi());
                }
            });

            holder.select_camideKildi_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (NAMAZ_CAMIDE_KILDI == item.getNerdeKildi()) {
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        item.setNerdeKildi(NAMAZ_NOT_SELECTED);
                        holder.select_state_txt.setText(R.string.not_prayed);
                        holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                        holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                        holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                    } else {
                        holder.namazTimes_layOut.setLayoutParams(new LinearLayout.LayoutParams((screenWidth - holder.nerdeKildi_layout.getMinimumWidth()), LinearLayout.LayoutParams.MATCH_PARENT));
                        item.setNerdeKildi(NAMAZ_CAMIDE_KILDI);
                        holder.select_state_txt.setText(R.string.mosque_prayed);
                        holder.select_camideKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_missed));
                        holder.select_gecKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_late));
                        holder.select_kildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz));
                        YoYo.with(Techniques.RotateOutDownRight).duration(5).playOn(holder.namaz_selection);
                        holder.nerdeKildi_layout.setVisibility(View.VISIBLE);
                        holder.nerdeKildi_img.setImageDrawable(getResources().getDrawable(R.drawable.prayed_namaz_in_mosque));
                    }
                    updateNamazInfo(position, item.getNerdeKildi());
                }
            });


            return convertView;
        }

    }

    private void setColors(int position, View convertView, ViewHolder holder) {
        if (0 == position) {
            convertView.setBackgroundResource(R.color.colorSabah);
            holder.select_gecKildi_img.setBackgroundColor(getResources().getColor(R.color.colorSabah0));
            holder.select_kildi_img.setBackgroundColor(getResources().getColor(R.color.colorSabah1));
            holder.select_camideKildi_img.setBackgroundColor(getResources().getColor(R.color.colorSabah2));
        } else if (1 == position) {
            convertView.setBackgroundResource(R.color.colorOgle);
            holder.select_gecKildi_img.setBackgroundColor(getResources().getColor(R.color.colorOgle0));
            holder.select_kildi_img.setBackgroundColor(getResources().getColor(R.color.colorOgle1));
            holder.select_camideKildi_img.setBackgroundColor(getResources().getColor(R.color.colorOgle2));
        } else if (2 == position) {
            convertView.setBackgroundResource(R.color.colorIkindi);
            holder.select_gecKildi_img.setBackgroundColor(getResources().getColor(R.color.colorIkindi0));
            holder.select_kildi_img.setBackgroundColor(getResources().getColor(R.color.colorIkindi1));
            holder.select_camideKildi_img.setBackgroundColor(getResources().getColor(R.color.colorIkindi2));
        } else if (3 == position) {
            convertView.setBackgroundResource(R.color.colorAksam);
            holder.select_gecKildi_img.setBackgroundColor(getResources().getColor(R.color.colorAksam0));
            holder.select_kildi_img.setBackgroundColor(getResources().getColor(R.color.colorAksam1));
            holder.select_camideKildi_img.setBackgroundColor(getResources().getColor(R.color.colorAksam2));
        } else if (4 == position) {
            convertView.setBackgroundResource(R.color.colorYatsi);
            holder.select_gecKildi_img.setBackgroundColor(getResources().getColor(R.color.colorYatsi0));
            holder.select_kildi_img.setBackgroundColor(getResources().getColor(R.color.colorYatsi1));
            holder.select_camideKildi_img.setBackgroundColor(getResources().getColor(R.color.colorYatsi2));
        }
    }

    public void updateNamazInfo(int position, int selection) {
        if (0 == position) {
            db.updateNamazVakitDeger(Integer.valueOf(anaMenuDate), NAMAZ_SABAH, selection);
        } else if (1 == position) {
            db.updateNamazVakitDeger(Integer.valueOf(anaMenuDate), NAMAZ_OGLE, selection);
        } else if (2 == position) {
            db.updateNamazVakitDeger(Integer.valueOf(anaMenuDate), NAMAZ_IKINDI, selection);
        } else if (3 == position) {
            db.updateNamazVakitDeger(Integer.valueOf(anaMenuDate), NAMAZ_AKSAM, selection);
        } else if (4 == position) {
            db.updateNamazVakitDeger(Integer.valueOf(anaMenuDate), NAMAZ_YATSI, selection);
        }
    }

    public void setAnaMenuDate(String anaMenuDt) throws ParseException {

        SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd");
        Date menuDate = formatDate.parse(anaMenuDt);
        formatDate.applyPattern("MMMM , yyyy-d-EEEE");
        String dt = formatDate.format(menuDate);

        namaz_tarih_txt.setText(dt.split("-")[0]);
        namaz_gun_sayi_txt.setText(dt.split("-")[1]);
        namaz_gun_metin_txt.setText(dt.split("-")[2]);
    }

    private static class ViewHolder {

        private View view;

        private TextView namazVakti;
        private ImageView nerdeKildi_img;

        private LinearLayout nerdeKildi_layout;
        private LinearLayout namaz_selection;
        private LinearLayout selection;
        private LinearLayout namazTimes_layOut;

        private ImageView select_gecKildi_img;
        private ImageView select_kildi_img;
        private TextView select_state_txt;
        private ImageView select_camideKildi_img;

        public ViewHolder(View view) {
            this.view = view;
            namazVakti = (TextView) view.findViewById(R.id.txt_namazTimes);
            nerdeKildi_img = (ImageView) view.findViewById(R.id.nerdeKildi_img);
            nerdeKildi_layout = (LinearLayout) view.findViewById(R.id.nerdeKildi_layout);
            namaz_selection = (LinearLayout) view.findViewById(R.id.namaz_selection);
            selection = (LinearLayout) view.findViewById(R.id.selection);
            namazTimes_layOut = (LinearLayout) view.findViewById(R.id.txt_namazTimes_layOut);

            select_gecKildi_img = (ImageView) view.findViewById(R.id.select_gecKildi_img);
            select_kildi_img = (ImageView) view.findViewById(R.id.select_kildi_img);
            select_state_txt = (TextView) view.findViewById(R.id.select_state_txt);
            select_camideKildi_img = (ImageView) view.findViewById(R.id.select_camideKildi_img);
        }
    }
}
