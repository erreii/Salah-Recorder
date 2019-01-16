package unknown.salah.pages;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Asus on 2/25/2017.
 */

public class RaporAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public RaporAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                RaporHaftalik tab1 = new RaporHaftalik();
                return tab1;
            case 1:
                RaporAylik tab2 = new RaporAylik();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}