package data.hci.gdatawatch.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import data.hci.gdatawatch.Activity.PageActivity;
import data.hci.gdatawatch.R;

/**
 * Created by user on 2016-06-08.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    Context mContext;

    //PagerAdapter Contstruct
    public PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    //set the page fragment
    @Override
    public Fragment getItem(int position) {
        Fragment currFragment = new PageActivity.PlaceholderFragment().newInstance(position+1);

        switch (position){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
        return currFragment;
    }

    //set the page title
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.menu_schedule);
            case 1:
                return mContext.getResources().getString(R.string.menu_test);
            case 2:
                return mContext.getResources().getString(R.string.menu_search);
            case 3:
                return mContext.getResources().getString(R.string.menu_setting);
        }

        return null;
    }


    //set the page number
    @Override
    public int getCount() {
        return 4;
    }
}
