package com.example.durobelacic.moviemania.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.durobelacic.moviemania.Api.MovieService;
import com.example.durobelacic.moviemania.Fragments.ResultsFragment;
import com.example.durobelacic.moviemania.Fragments.WatchlistFragment;
import com.example.durobelacic.moviemania.Models.Genres;
import com.example.durobelacic.moviemania.Models.GenresResults;
import com.example.durobelacic.moviemania.Models.Result;
import com.example.durobelacic.moviemania.MovieManiaActivity;
import com.example.durobelacic.moviemania.R;
import com.example.durobelacic.moviemania.Utils.FirebaseDataHelper;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchlistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // View Types
    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w200";

    private List<Result> movieResults;

    private Context context;

    private final RequestManager glide;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;
    private boolean isVisible = false;

    private MoviePaginationAdapterCallback mCallback;

    private String errorMsg;

    WatchlistFragment watchlistFragment;

    private BottomSheetDialog bottomSheetDialog;
    private View view;
    private String user;
    private MovieService movieService;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private List<Result> watchlist = new ArrayList<>();

    public WatchlistAdapter(Context context, RequestManager glide, BottomSheetDialog bottomSheetDialog, View view, String user,
                            WatchlistFragment watchlistFragment, MovieService movieService) {
        this.context = context;
        this.mCallback = (MoviePaginationAdapterCallback) context;
        movieResults = new ArrayList<>();
        this.glide = glide;
        this.bottomSheetDialog = bottomSheetDialog;
        this.view = view;
        this.user = user;
        this.watchlistFragment = watchlistFragment;
        this.movieService = movieService;
    }

    public List<Result> getMovieResults() {
        return movieResults;
    }

    public void setMovies(List<Result> movieResults) {

        this.movieResults = movieResults;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.item_watchlist, parent, false);
                viewHolder = new WatchlistAdapter.MovieVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new WatchlistAdapter.LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Result result = movieResults.get(position); // Movie
        switch (getItemViewType(position)) {

            case ITEM:
                if(result.getUser() != null)
                {
                    if(result.getUser().equals(user))
                    {
                        final WatchlistAdapter.MovieVH movieVH = (WatchlistAdapter.MovieVH) holder;
                        watchlist.add(result);
                        movieVH.mNoMovies.setVisibility(View.GONE);
                        movieVH.mMovieTitle.setText(result.getTitle());
                        movieVH.mYear.setText(formatYearLabel(result));
                        movieVH.mMovieDesc.setText(result.getOverview());

                        glide
                                .load(BASE_URL_IMG + result.getPosterPath())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        movieVH.mProgress.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        movieVH.mProgress.setVisibility(View.GONE);
                                        return false;
                                    }
                                }).diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(movieVH.mPosterImg);
                    }
                    else if(watchlist.isEmpty() && position == movieResults.size() - 1){
                        System.out.println("Uslo");
                        final WatchlistAdapter.MovieVH movieVH = (WatchlistAdapter.MovieVH) holder;
                        movieVH.mNoMovies.setVisibility(View.VISIBLE);
                        movieVH.mLinearLayout.setVisibility(View.GONE);
                        movieVH.mCardView.setVisibility(View.GONE);
                        movieVH.mProgress.setVisibility(View.GONE);
                    } else {
                        setVisibility(holder);
                    }
                }
                else {
                    return;
                }
                break;

            case LOADING:
                WatchlistAdapter.LoadingVH loadingVH = (WatchlistAdapter.LoadingVH) holder;
                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                TextView movieTitle = view.findViewById(R.id.movie_title);
                TextView movieYear = view.findViewById(R.id.movie_year);
                TextView movieLang = view.findViewById(R.id.movie_lang);
                TextView movieDesc = view.findViewById(R.id.movie_desc);
                TextView movieRating = view.findViewById(R.id.mRating);
                TextView movieGenres = view.findViewById(R.id.mGenres);
                Button btnRemoveFromList = view.findViewById(R.id.btnRemoveFromList);

                movieDesc.setMovementMethod(new ScrollingMovementMethod());

                movieTitle.setText(result.getTitle());
                movieYear.setText(formatYearLabelYear(result));
                movieLang.setText(result.getOriginalLanguage().toUpperCase());
                movieDesc.setText(result.getOverview());
                movieRating.setText(result.getVoteAverage().toString()+"/10");

                List<String> lGenres = new ArrayList<>();
                callGenres().enqueue(new Callback<Genres>() {
                    @Override
                    public void onResponse(Call<Genres> call, Response<Genres> response) {
                        List<GenresResults> genresResults = fetchResult(response);

                        for(GenresResults result1 : genresResults){
                            if(result.getGenreIds().contains(result1.getId()))
                            {
                                lGenres.add(result1.getName());
                            }
                        }
                        String sGenres = "";
                        for(String genre : lGenres){
                            sGenres += genre + ", ";
                        }

                        System.out.println(sGenres);

                        sGenres = sGenres.replaceAll(", $", "");
                        movieGenres.setText(sGenres);
                    }

                    @Override
                    public void onFailure(Call<Genres> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

                RoundedImageView roundedImageView = view.findViewById(R.id.mPosterImg);
                glide
                        .load(BASE_URL_IMG + result.getPosterPath())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(roundedImageView);

                btnRemoveFromList.setOnClickListener(v2 -> {
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("Watchlist");
                    String sTrimUser = user.replace(".","").replace("#", "").replace("[","").replace("]", "");
                    reference.child(result.getId().toString()+"_"+sTrimUser).removeValue();
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    watchlistFragment.doRefresh();
                    String deletePage = String.format("You have deleted "+result.getTitle()+" from your list!");
                    Toast.makeText(v2.getContext(), ""+deletePage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieResults == null ? 0 : movieResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM;
        } else {
            return (position == movieResults.size() - 1 && isLoadingAdded ) ? LOADING : ITEM;
        }
    }

    /*
        Helperi
   _________________________________________________________________________________________________
    */

    /*
     * Godina
     * */
    public String formatYearLabel(Result result) {
        String year = "";
        if(result.getReleaseDate() != null)
        {
            year = result.getReleaseDate().substring(0, 4)  // samo godina
                    + " | "
                    + result.getOriginalLanguage().toUpperCase();
        }

        return year;
    }

    public String formatYearLabelYear(Result result) {
        String year = "";
        if(result.getReleaseDate() != null)
        {
            year = result.getReleaseDate().substring(0, 4);  // samo godina
        }

        return year;
    }

    /*
     *
     * */
    public void setVisibility(RecyclerView.ViewHolder holder){
        holder.itemView.setVisibility(View.GONE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Result r) {
        if(!movieResults.contains(r))
        {
            movieResults.add(r);
            notifyItemInserted(movieResults.size() - 1);
        }
    }

    public void addAll(List<Result> movieResults) {
        Set<Result> results = new HashSet<>(movieResults);
        for (Result result : movieResults) {
            if(!results.add(result))
            {
                add(result);
            }
        }
    }

    public void remove(Result r) {
        int position = movieResults.indexOf(r);
        if (position > -1) {
            movieResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Result());
    }

    public void addRemovingFooter() {
        isLoadingAdded = false;

        int position = movieResults.size() - 1;
        Result result = getItem(position);

        if (result != null) {
            movieResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Result getItem(int position) {
        return movieResults.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(movieResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

   /*
   View Holderi
   _________________________________________________________________________________________________
    */

    /*
     * Lista
     * */
    protected class MovieVH extends RecyclerView.ViewHolder {
        private TextView mMovieTitle;
        private TextView mMovieDesc;
        private TextView mYear; // displays "year | language"
        private ImageView mPosterImg;
        private ProgressBar mProgress;
        private TextView mNoMovies;
        private LinearLayout mLinearLayout;
        private CardView mCardView;

        public MovieVH(View itemView) {
            super(itemView);

            mMovieTitle = itemView.findViewById(R.id.movie_title);
            mMovieDesc = itemView.findViewById(R.id.movie_desc);
            mYear = itemView.findViewById(R.id.movie_year);
            mPosterImg = itemView.findViewById(R.id.movie_poster);
            mProgress = itemView.findViewById(R.id.movie_progress);
            mNoMovies = itemView.findViewById(R.id.noResults);
            mLinearLayout = itemView.findViewById(R.id.movie_linear_layout);
            mCardView = itemView.findViewById(R.id.movie_card_view);
        }
    }

    protected class NoItemVH extends RecyclerView.ViewHolder{
        private TextView txtNoResults;

        public NoItemVH(@NonNull View itemView) {
            super(itemView);

            txtNoResults = itemView.findViewById(R.id.noResults);
        }
    }


    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        public LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

    private Call<Genres> callGenres(){
        return movieService.getGenres();
    }

    private List<GenresResults> fetchResult(Response<Genres> response) {
        Genres genres = response.body();
        return genres.getGenreResults();
    }
}

