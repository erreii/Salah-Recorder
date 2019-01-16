package unknown.salah.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "namazTakip.db";
    public static final int VERSION = 5;

    public static final String NAMAZ_TABLE_NAME = "namaz";
    public static final String NAMAZ_COLUMN_TARIH = "namaz_tarih";
    public static final String NAMAZ_COLUMN_VAKIT = "namaz_vakit";
    public static final String NAMAZ_COLUMN_VAKIT_DEGER = "namaz_vakit_deger";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //Create Namaz DB
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + NAMAZ_TABLE_NAME + " (" + NAMAZ_COLUMN_TARIH + " integer, " + NAMAZ_COLUMN_VAKIT + " integer, " + NAMAZ_COLUMN_VAKIT_DEGER + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NAMAZ_TABLE_NAME);
        onCreate(db);
    }

    //Insert Namaz
    public boolean insertNamaz(int namazTarih, int namazVakit, int vakitDeger) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAMAZ_COLUMN_TARIH, namazTarih);
        contentValues.put(NAMAZ_COLUMN_VAKIT, namazVakit);
        contentValues.put(NAMAZ_COLUMN_VAKIT_DEGER, vakitDeger);
        db.insert(NAMAZ_TABLE_NAME, null, contentValues);
        return true;
    }

    //Update Namaz Vakit Degeri
    public boolean updateNamazVakitDeger(int namazTarih, int namazVakit, int vakitDeger) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAMAZ_COLUMN_VAKIT_DEGER, vakitDeger);
        db.update(NAMAZ_TABLE_NAME, contentValues, NAMAZ_COLUMN_TARIH + " = ? AND " + NAMAZ_COLUMN_VAKIT + " = ?", new String[]{String.valueOf(namazTarih), String.valueOf(namazVakit)});
        return true;
    }

    public Cursor getNamazInfoWithDate(int namazTarih) {

        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("SELECT * FROM " + NAMAZ_TABLE_NAME + " WHERE " + NAMAZ_COLUMN_TARIH + " =" + namazTarih, null);
    }

    public Cursor getInfoAll(int namazTarih,int namazVakit) {

        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("SELECT * FROM " + NAMAZ_TABLE_NAME + " WHERE " + NAMAZ_COLUMN_TARIH + " =" + namazTarih + " AND " + NAMAZ_COLUMN_VAKIT + " =" + namazVakit + ";", null);
    }

    public Cursor getNamazInfoWeekly(int namazDateFROM,int namazDateTo) {

        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("SELECT * FROM " + NAMAZ_TABLE_NAME + " WHERE " + NAMAZ_COLUMN_TARIH + " >= " + namazDateFROM +" and " + NAMAZ_COLUMN_TARIH + " <= " + namazDateTo +" ORDER BY "+ NAMAZ_COLUMN_TARIH+" DESC;", null);
    }

    public Cursor getNamazInfoMonthly(int namazDate,int nextMonth) {

        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("SELECT " + NAMAZ_COLUMN_VAKIT + " ," + NAMAZ_COLUMN_VAKIT_DEGER + " FROM " + NAMAZ_TABLE_NAME + " WHERE " + NAMAZ_COLUMN_TARIH + " >= " + namazDate + " and " + NAMAZ_COLUMN_TARIH + " < " + nextMonth +" ORDER BY "+ NAMAZ_COLUMN_TARIH+" DESC;", null);
    }

    public Cursor noOfDayNamaz(int namazTarih) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + NAMAZ_COLUMN_TARIH + " FROM " + NAMAZ_TABLE_NAME + " WHERE " + NAMAZ_COLUMN_TARIH + "=" + namazTarih + ";", null);
        return res;
    }

    public int noOfNamaz() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numberOfRows = (int) DatabaseUtils.queryNumEntries(db, null);
        return numberOfRows;
    }

    public Cursor getAllNamazInfo(){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("SELECT * FROM " + NAMAZ_TABLE_NAME + " ORDER BY " + NAMAZ_COLUMN_TARIH +" DESC;", null);
    }

    //Delete Namaz Deger
    public Integer deleteNamazDeger(int namazTarih) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(NAMAZ_TABLE_NAME,
                "namazTarih = ? ",
                new String[]{String.valueOf(namazTarih)});
    }

}
