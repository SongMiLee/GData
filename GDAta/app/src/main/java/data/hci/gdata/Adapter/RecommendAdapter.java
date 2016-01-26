package data.hci.gdata.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import data.hci.gdata.R;

/**
 * Created by user on 2016-01-26.
 */
public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.ViewHolder> {
    List<RecommendItem> item;
    Handler handler;
    Bitmap bitmap=null;

    public RecommendAdapter(List<RecommendItem> recommendItems){
        item = recommendItems;
        handler = new Handler();
    }
    @Override
    public RecommendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_cardview, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecommendAdapter.ViewHolder holder, int position) {
        holder.title.setText(item.get(position).getTitle());
        getBitmap(holder, position);
    }

    public void getBitmap(final RecommendAdapter.ViewHolder holder,final int position){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try{
                    URL url = new URL(item.get(position).getImage());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();

                    bitmap = BitmapFactory.decodeStream(conn.getInputStream());

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(conn != null)
                        conn.disconnect();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewHolder tmpHolder = holder;
                        tmpHolder.img.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
    }

    /**
     * 사이즈 반환
     * */
    @Override
    public int getItemCount() {
        return item.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView title;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView)itemView.findViewById(R.id.card_img);
            title = (TextView)itemView.findViewById(R.id.card_title);
            cardView = (CardView)itemView.findViewById(R.id.cardview);
        }
    }
}
