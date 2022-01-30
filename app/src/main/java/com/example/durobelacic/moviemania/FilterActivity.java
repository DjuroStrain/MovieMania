package com.example.durobelacic.moviemania;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.example.durobelacic.moviemania.Fragments.FilterFragment;
import com.example.durobelacic.moviemania.Fragments.MovieManiaFragment;
import com.example.durobelacic.moviemania.Fragments.ResultsFragment;
import com.google.android.material.navigation.NavigationView;

public class FilterActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private Toolbar toolbar;
    private ImageView imgMenu, imgBtnBack;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_filter);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_view, new FilterFragment());
        fragmentTransaction.commit();

        imgMenu = (ImageView) findViewById(R.id.drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        imgMenu.setOnClickListener(v ->{
            drawerLayout.openDrawer(Gravity.LEFT);
        });

        FilterFragment filterFragment = new FilterFragment();
        ResultsFragment resultsFragment = new ResultsFragment();
        MovieManiaFragment movieManiaFragment = new MovieManiaFragment();

        imgBtnBack = (ImageView) findViewById(R.id.btnBack);
        imgBtnBack.setOnClickListener(v -> {

            System.out.println(getCurrentFragment());
            if(getCurrentFragment().equals("FilterFragment"))
            {
                Intent movieActivityIntent = new Intent(getApplicationContext(), MovieManiaActivity.class);
                startActivity(movieActivityIntent);
            }
            else if(getCurrentFragment().equals("ResultsFragment")) {
                FragmentTransaction fragmentTransaction2 = this.getSupportFragmentManager().beginTransaction();
                fragmentTransaction2.replace(R.id.container_view, filterFragment);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.commit();
            }
        });



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.nav_top_rated){
//                    Intent moviesActivityIntent = new Intent(getApplicationContext(), MovieManiaActivity.class);
//                    startActivity(moviesActivityIntent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.container_view, movieManiaFragment);
                    fragmentTransaction1.addToBackStack(null);
                    fragmentTransaction1.commit();
                }
                else if(id == R.id.nav_filter){

                }
                else if(id == R.id.nav_log_out){
                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainActivityIntent);
                }
                return true;
            }
        });
    }

    public String getCurrentFragment(){
        return this.getSupportFragmentManager().findFragmentById(R.id.container_view).getClass().getSimpleName();
    }
}