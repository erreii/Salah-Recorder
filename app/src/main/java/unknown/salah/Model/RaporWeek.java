package unknown.salah.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaporWeek {
    private int tarih;

    private Map<Integer,List<String>> values;

    public RaporWeek(int tarih, Map<Integer,List<String>> allValues) {
        this.tarih = tarih;
        values = new HashMap<Integer,List<String>>();
        values.putAll(allValues);
    }

    public int getTarih() {
        return tarih;
    }

    public void setTarih(int tarih) {
        this.tarih = tarih;
    }

    public Map<Integer, List<String>> getValues() {
        return values;
    }

    public void setValues(Map<Integer, List<String>> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "RaporWeek{" +
                "tarih=" + tarih +
                ", values=" + values +
                '}';
    }
}
