package com.example.user.heartbeatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MemorandumActivity extends AppCompatActivity {

    private MySQLiteOpenHelper sqliteHelper;
    private SpotAdapter spotAdapter;
    private RecyclerView rvSpots;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorandum);
        //如果沒有SQLiteOpenHelper物件，就建立一個新的
        if (sqliteHelper == null) {
            sqliteHelper = new MySQLiteOpenHelper(this);
        }
        rvSpots = (RecyclerView) findViewById(R.id.rvSpots);
        rvSpots.setLayoutManager(new LinearLayoutManager(this));

    }

    //設定選單
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.memorandum_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
         int intent = item.getItemId();
         if(intent == R.id.memorandum_explain){
             Intent it = new Intent (this, Memorandum_explain.class);
             startActivity(it);
         }
         return true;
    }
    @Override
    protected void onStart() {
        super.onStart();
        List<Spot> spotList = getSpotList();
        if (spotList.size() <= 0) {
            Toast.makeText(
                    this, R.string.text_NoDataFound, Toast.LENGTH_SHORT
            ).show();
        }
        //如果沒有SpotAdapter物件，就建立一個新的。如果已經有就更新資料內容後呼叫
        //notifyDataSetChenged()刷新畫面
        if (spotAdapter == null) {
            spotAdapter = new SpotAdapter(this, spotList);
            rvSpots.setAdapter(spotAdapter);
        } else {
            spotAdapter.setSpotList(spotList);
            spotAdapter.notifyDataSetChanged();
        }

    }

    //呼叫MySQLiteOpenHelper.getAllSpots()回傳所有旅遊景點
    public List<Spot> getSpotList() {
        return sqliteHelper.getAllSpots();
    }

    //連結到Insert畫面
    public void insert (View view){
        Intent it = new Intent(this, InsertActivity.class);
        startActivity(it);
    }


    //實作RecyclerView.Adapter
    private class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {
        Context context;
        List<Spot> spotList;

        void setSpotList(List<Spot> spotList) {
            this.spotList = spotList;
        }

        SpotAdapter(Context context, List<Spot> spotList) {
            this.context = context;
            this.spotList = spotList;
        }

        class SpotViewHolder extends RecyclerView.ViewHolder {
            ImageView ivSpot;
            TextView tvName, tvPhone, tvAddress, tvWeb;

            SpotViewHolder(View itemView) {
                super(itemView);
                ivSpot = (ImageView) itemView.findViewById(R.id.ivSpot);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvPhone = (TextView) itemView.findViewById(R.id.tvPhone);
                tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
                tvWeb = (TextView) itemView.findViewById(R.id.tvWeb);
            }
        }

        @Override
        public int getItemCount() {
            return spotList.size();
        }

        @Override
        public SpotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemView = layoutInflater.inflate(R.layout.item_view, parent, false);
            return new SpotViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SpotViewHolder holder, int position) {
            final Spot spot = spotList.get(position);
            //如果資料庫內沒有儲存景點照片，就顯示預設圖示;如果有則顯示該照片
            if (spot.getImage() == null) {
                holder.ivSpot.setImageResource(R.drawable.ic_menu_camera);
            } else {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        spot.getImage(), 0, spot.getImage().length);
                holder.ivSpot.setImageBitmap(bitmap);
            }
            holder.tvName.setText(spot.getName());
            holder.tvPhone.setText(spot.getPhone());
            holder.tvAddress.setText(spot.getAddress());
            holder.tvWeb.setText(spot.getWeb());
            //點擊資料列就開啟修改畫面，並將景點ID傳遞過去
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UpdateActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", spot.getId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            //長按則刪除該資料列
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int count = sqliteHelper.deleteById(spot.getId());
                    Toast.makeText(context, count + " " + getString(R.string.msg_RowDeleted),
                            Toast.LENGTH_SHORT).show();
                    //重新取得資料後呼叫notifyDataSetChanged()重刷RecyclerView畫面
                    spotList = sqliteHelper.getAllSpots();
                    notifyDataSetChanged();
                    return true;
                }
            });
        }
    }
    //此頁面結束時關閉SQLiteOpenHelper物件
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sqliteHelper != null) {
            sqliteHelper.close();
        }
    }


}
