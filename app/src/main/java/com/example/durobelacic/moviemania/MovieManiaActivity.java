package com.example.durobelacic.moviemania;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.durobelacic.moviemania.Adapters.MoviePaginationAdapter;
import com.example.durobelacic.moviemania.Api.MovieAPI;
import com.example.durobelacic.moviemania.Api.MovieService;
import com.example.durobelacic.moviemania.Fragments.FilterFragment;
import com.example.durobelacic.moviemania.Fragments.LoginFragment;
import com.example.durobelacic.moviemania.Fragments.MovieManiaFragment;
import com.example.durobelacic.moviemania.Fragments.RegisterFragment;
import com.example.durobelacic.moviemania.Fragments.ResultsFragment;
import com.example.durobelacic.moviemania.Fragments.WatchlistFragment;
import com.example.durobelacic.moviemania.Models.Result;
import com.example.durobelacic.moviemania.Models.TopRatedMovies;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;
import com.example.durobelacic.moviemania.Utils.PaginationScrollListener;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.concurrent.TimeoutException;

import okhttp3.Cache;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieManiaActivity extends AppCompatActivity {

    ImageView imgMenu, imgBack;
    DrawerLayout drawerLayout;
    LinearLayout linearLayout;
    String sUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_moivemania);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container_view, new MovieManiaFragment());
        fragmentTransaction.commit();


        imgMenu = (ImageView) findViewById(R.id.drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        imgMenu.setOnClickListener(v ->{
            drawerLayout.openDrawer(Gravity.LEFT);
        });

        Bundle bundle1 = getIntent().getExtras();
        String sUser = bundle1.getString("user");

        NavigationView navigationView2 = (NavigationView)findViewById(R.id.nav_view);
        View headerView = navigationView2.getHeaderView(0);
        TextView userLog = headerView.findViewById(R.id.userLog);
        userLog.setText(sUser);
        navigationView2.getMenu().getItem(0).setChecked(true);
        imgBack = (ImageView)findViewById(R.id.btnBack);
        imgBack.setOnClickListener(v -> {
            if(getCurrentFragment().equals("MovieManiaFragment"))
            {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivityIntent);
            }
            else if(getCurrentFragment().equals("FilterFragment"))
            {
                MovieManiaFragment movieManiaFragment  = new MovieManiaFragment();
                FragmentTransaction fragmentTransaction2 = this.getSupportFragmentManager().beginTransaction();
                fragmentTransaction2.replace(R.id.container_view, movieManiaFragment);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.commit();
                navigationView2.getMenu().getItem(0).setChecked(true);
            }
            else if (getCurrentFragment().equals("WatchlistFragment")){
                MovieManiaFragment movieManiaFragment = new MovieManiaFragment();
                FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.container_view, movieManiaFragment);
                fragmentTransaction1.addToBackStack(null);
                fragmentTransaction1.commit();
                navigationView2.getMenu().getItem(0).setChecked(true);
            }
            else if(getCurrentFragment().equals("ResultsFragment")) {
                FilterFragment filterFragment = new FilterFragment();
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
                    MovieManiaFragment movieManiaFragment = new MovieManiaFragment();
                    FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.container_view, movieManiaFragment);
                    fragmentTransaction1.addToBackStack(null);
                    fragmentTransaction1.commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(id == R.id.nav_filter){
//                    Intent filterActivityIntent = new Intent(getApplicationContext(), FilterActivity.class);
//                    startActivity(filterActivityIntent);
                    FilterFragment filterFragment = new FilterFragment();
                    FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.container_view, filterFragment);
                    fragmentTransaction1.addToBackStack(null);
                    fragmentTransaction1.commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(id == R.id.nav_watchlist){
//                    Intent filterActivityIntent = new Intent(getApplicationContext(), FilterActivity.class);
//                    startActivity(filterActivityIntent);
                    WatchlistFragment watchlistFragment = new WatchlistFragment();
                    FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.container_view, watchlistFragment);
                    fragmentTransaction1.addToBackStack(null);
                    fragmentTransaction1.commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(id == R.id.nav_log_out){
                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainActivityIntent);
                }
                return true;
            }
        });

//        final Bundle bundle = getIntent().getExtras();
//        sUser = bundle.getString("user");
//
//        Bundle bundle1 = new Bundle();
//        bundle1.putString("user", sUser);
//        System.out.println("User: " +sUser);
//        ResultsFragment resultsFragment = new ResultsFragment();
//        resultsFragment.setArguments(bundle1);

        ResultsFragment resultsFragment = new ResultsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("year", 2017);
        resultsFragment.setArguments(bundle);
    }

    public String getCurrentFragment(){
        return this.getSupportFragmentManager().findFragmentById(R.id.container_view).getClass().getSimpleName();
    }
}