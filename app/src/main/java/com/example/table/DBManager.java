package com.example.table;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.table.adapter.DataSender;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private Query mQuery;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private int category_ads_counter = 0;
    private String[] catedory_ads = {"Машины", "Компьютеры", "Смартфоны", "Бытовая техника"};

    public DBManager(DataSender dataSender) {
        this.dataSender = dataSender;
        newPostList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
    }

    public void getDataFromDb(String path) {
        DatabaseReference dRef = db.getReference(path);
        mQuery = dRef.orderByChild("ad/time");
        readDataUpdate();
    }

    public void getMyDataFromDb(String uid) {
        if(newPostList.size()>0)newPostList.clear();
        DatabaseReference dbRef = db.getReference(catedory_ads[0]);
        mQuery = dbRef.orderByChild("ad/uid").equalTo(uid);
        readMyAdsDataUpdate(uid);
        category_ads_counter++;
    }

    public void readDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(newPostList.size() > 0) newPostList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NewPost newPost = ds.child("ad").getValue(NewPost.class);
                    newPostList.add(newPost);
                }
                dataSender.onDataRecived(newPostList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void readMyAdsDataUpdate(final String uid) {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NewPost newPost = ds.child("ad").getValue(NewPost.class);
                    newPostList.add(newPost);
                }
                if(category_ads_counter > 3) {
                    dataSender.onDataRecived(newPostList);
                    newPostList.clear();
                    category_ads_counter = 0;
                } else {
                    DatabaseReference dRef = db.getReference(catedory_ads[category_ads_counter]);
                    mQuery = dRef.orderByChild("ad/uid").equalTo(uid);
                    readMyAdsDataUpdate(uid);
                    category_ads_counter++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
