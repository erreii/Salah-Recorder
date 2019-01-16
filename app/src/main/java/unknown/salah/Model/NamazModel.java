package unknown.salah.Model;

/**
 * Created by Asus on 2/18/2017.
 */

public class NamazModel {
    private int namazTarih;
    //1:Sabah,2:Ogle,3:Ikindi,4:Aksam,5:Yatsi
    private int namazVakit;
    //0:Empty,1:Gec Kildi,2:Kildi,3:Camide Kildi
    private int nerdeKildi;

    public NamazModel(int namazTarih,int namazVakit, int nerdeKildi) {
        this.namazTarih = namazTarih;
        this.namazVakit = namazVakit;
        this.nerdeKildi = nerdeKildi;
    }

    public int getNamazTarih() {
        return namazTarih;
    }

    public void setNamazTarih(int namazTarih) {
        this.namazTarih = namazTarih;
    }

    public int getNamazVakit() {
        return namazVakit;
    }

    public void setNamazVakit(int namazVakit) {
        this.namazVakit = namazVakit;
    }

    public int getNerdeKildi() {
        return nerdeKildi;
    }

    public void setNerdeKildi(int nerdeKildi) {
        this.nerdeKildi = nerdeKildi;
    }

    @Override
    public String toString() {
        return "NamazModel{" +
                "namazTarih=" + namazTarih +
                ", namazVakit=" + namazVakit +
                ", nerdeKildi=" + nerdeKildi +
                '}';
    }
}
