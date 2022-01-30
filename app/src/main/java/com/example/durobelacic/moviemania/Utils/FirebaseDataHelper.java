package com.example.durobelacic.moviemania.Utils;

import androidx.annotation.NonNull;

import com.example.durobelacic.moviemania.Models.Result;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDataHelper {

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private List<Result> results = new ArrayList<>();

    public interface DataStatus {
        void DataIsLoaded(List<Result> results, List<String> keys);
    }

    public FirebaseDataHelper() {
        database = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Watchlist");
    }

    public void readResults(final DataStatus dataStatus){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                results.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : snapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    Result result = keyNode.getValue(Result.class);
                    results.add(result);
                }
                dataStatus.DataIsLoaded(results, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
