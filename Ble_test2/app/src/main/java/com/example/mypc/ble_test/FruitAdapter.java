package com.example.mypc.ble_test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by MyPC on 2016/1/9.
 */
public class FruitAdapter extends ArrayAdapter<Fruit>{
    private int resourceId;
    public FruitAdapter(Context context, int textViewResourceId,
                        List<Fruit> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Fruit fruit = getItem(position); // 获取当前项的Fruit实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        ImageView fruitImage = (ImageView) view.findViewById(R.id.fruit_image);
        TextView fruitName = (TextView) view.findViewById(R.id.fruit_name);
        TextView ftuitAddress = (TextView)view.findViewById(R.id.fruit_address);
        fruitImage.setImageResource(fruit.getImageId());
        fruitName.setText(fruit.getName());
        ftuitAddress.setText(fruit.getAddress());
        return view;
    }
}
