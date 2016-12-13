package jltechnologies.com.mysky.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import jltechnologies.com.mysky.ui.R;
import jltechnologies.com.mysky.weather.Day;

/**
 * Created by jeff on 12/12/16.
 */

public class DayAdapter extends BaseAdapter {

    private Context mContext;
    private Day[] mDays;

    public DayAdapter(Context context, Day[] days){
        mContext = context;
        mDays = days;
    }
    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int i) {
        return mDays[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;//not gonna use. Tags items for easy reference
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);
            holder.circleImageView = (ImageView) convertView.findViewById(R.id.circleImageView);

            convertView.setTag(holder);


            holder = (ViewHolder) convertView.getTag();


        Day day = mDays[i];
        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(day.getTemperatureMax()+"");
        if(i == 0)
        {
            holder.dayLabel.setText("Today");
        }
        else {
            holder.dayLabel.setText(day.getDayOfTheWeek());
        }
        holder.circleImageView.setImageResource(R.mipmap.bg_temperature);

        return convertView;
    }
    private static class ViewHolder {
        ImageView iconImageView;//public
        TextView temperatureLabel;
        TextView dayLabel;
        ImageView circleImageView;
    }
}
