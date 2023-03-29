package com.example.table.adapter;

import com.example.table.NewPost;

import java.util.List;

public interface DataSender {
    public void onDataRecived(List<NewPost> listData);
}
