package com.example.durobelacic.moviemania.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.durobelacic.moviemania.Api.MovieService;
import com.example.durobelacic.moviemania.Fragments.ResultsFragment;
import com.example.durobelacic.moviemania.Models.Genres;
import com.example.durobelacic.moviemania.Models.GenresResults;
import com.example.durobelacic.moviemania.Models.Result;
import com.example.durobelacic.moviemania.MovieManiaActivity;
import com.example.durobelacic.moviemania.R;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;
import com.example.durobelacic.moviemania.Utils.PageViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterMoviePaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MoviePaginationAdapterCallback{

    // View Types
    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int NO_ITEM = 2;

    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w200";

    private List<Result> movieResults;

    private Context context;

    private final RequestManager glide;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;
    private boolean isVisible = false;

    private MoviePaginationAdapterCallback mCallback;

    private String errorMsg;

    private BottomSheetDialog bottomSheetDialog;
    private View view;
    private String user, keyword, year, lang, genre;
    private Integer bottomRating, topRating;
    private MovieService movieService;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private List<Result> filteredMovies = new ArrayList<>();

    private List<Result> lastMovie = new ArrayList<>();

    private Integer totalPages;

    public FilterMoviePaginationAdapter(Context context, RequestManager glide, BottomSheetDialog bottomSheetDialog, View view, String user, String keyword,
                                        String year, String lang, String genre, int totalPages, int bottomRating, int topRating, MovieService movieService) {
        this.context = context;
        this.mCallback = (MoviePaginationAdapterCallback) context;
        movieResults = new ArrayList<>();
        this.glide = glide;
        this.bottomSheetDialog = bottomSheetDialog;
        this.view = view;
        this.user = user;
        this.keyword = keyword;
        this.year = year;
        this.lang = lang;
        this.genre = genre;
        this.totalPages = totalPages;
        this.bottomRating = bottomRating;
        this.topRating = topRating;
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
                View viewItem = inflater.inflate(R.layout.item_list, parent, false);
                viewHolder = new MovieVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
            case NO_ITEM:
                View viewNoItem = inflater.inflate(R.layout.no_item, parent, false);
                viewHolder = new NoItemVH(viewNoItem);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Result result = movieResults.get(position); // Movie
        int recordsNum = totalPages * 20;
        System.out.println("bottom: "+bottomRating+", top: "+topRating);
        switch (getItemViewType(position)) {
            case ITEM:
                if(topRating != 0)
                {
                    if(result.getTitle().toLowerCase().trim().contains(keyword.toLowerCase().trim()) && formatYearLabelYear(result).trim().contains(year.trim()) && result.getOriginalLanguage().trim().contains(lang.trim())
                            && result.getGenreIds().toString().trim().contains(genre.trim()) && result.getVoteAverage() > bottomRating && result.getVoteAverage() < topRating)
                    {
                        filteredMovies.add(result);
                        final MovieVH movieVH = (MovieVH) holder;
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
                    else{
                        setVisibility(holder);
                    }
                }
                else{
                    if(result.getTitle().toLowerCase().trim().contains(keyword.toLowerCase().trim()) && formatYearLabelYear(result).trim().contains(year.trim()) && result.getOriginalLanguage().trim().contains(lang.trim())
                            && result.getGenreIds().toString().trim().contains(genre.trim()) && result.getVoteAverage() > bottomRating)
                    {
                        filteredMovies.add(result);
                        final MovieVH movieVH = (MovieVH) holder;
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
                    else{
                        setVisibility(holder);
                    }
                }
                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;
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
            case NO_ITEM:
                NoItemVH noItemVH = (NoItemVH) holder;
                if(topRating != 0)
                {
                    if(result.getTitle().toLowerCase().trim().contains(keyword.toLowerCase().trim()) && formatYearLabelYear(result).trim().contains(year.trim()) && result.getOriginalLanguage().trim().contains(lang.trim())
                            && result.getGenreIds().toString().trim().contains(genre.trim()) && result.getVoteAverage() > bottomRating && result.getVoteAverage() < topRating) {
                        lastMovie.add(result);
                        System.out.println("title: "+result.getTitle());
                    }
                }
                else
                {
                    if(result.getTitle().toLowerCase().trim().contains(keyword.toLowerCase().trim()) && formatYearLabelYear(result).trim().contains(year.trim()) && result.getOriginalLanguage().trim().contains(lang.trim())
                            && result.getGenreIds().toString().trim().contains(genre.trim()) && result.getVoteAverage() > bottomRating) {
                        lastMovie.add(result);
                        System.out.println("title: "+result.getTitle());
                    }
                }

                if(filteredMovies.isEmpty() && lastMovie.isEmpty())
                {
                    noItemVH.txtNoResults.setVisibility(View.VISIBLE);
                    noItemVH.mPosterImg.setVisibility(View.GONE);
                    noItemVH.mLinearLayout.setVisibility(View.GONE);
                    noItemVH.mProgressBar.setVisibility(View.GONE);
                    noItemVH.mCardView.setVisibility(View.GONE);
                }
                else if(lastMovie.size() == 1)
                {
                        noItemVH.txtNoResults.setVisibility(View.GONE);

                        for(Result result1 : lastMovie){
                            System.out.println("Title: "+result1.getTitle());
                            noItemVH.mMovieTitle.setText(result1.getTitle());
                            noItemVH.mYear.setText(formatYearLabel(result1));
                            noItemVH.mMovieDesc.setText(result1.getOverview());

                            glide
                                    .load(BASE_URL_IMG + result1.getPosterPath())
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            //.mProgress.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            //noItemVH.mProgress.setVisibility(View.GONE);
                                            return false;
                                        }
                                    }).diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(noItemVH.mPosterImg);
                        }
                }
                else {
                    noItemVH.txtNoResults.setVisibility(View.GONE);
                    noItemVH.mPosterImg.setVisibility(View.GONE);
                    noItemVH.mLinearLayout.setVisibility(View.GONE);
                    noItemVH.mProgressBar.setVisibility(View.GONE);
                    noItemVH.mCardView.setVisibility(View.GONE);
                }
                System.out.println("NO_ITEM");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
                //View view = LayoutInflater.from(context).inflate(R.layout.movie_details_dialog, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                TextView movieTitle = view.findViewById(R.id.movie_title);
                TextView movieYear = view.findViewById(R.id.movie_year);
                TextView movieLang = view.findViewById(R.id.movie_lang);
                TextView movieDesc = view.findViewById(R.id.movie_desc);
                TextView movieRating = view.findViewById(R.id.mRating);
                TextView movieGenres = view.findViewById(R.id.mGenres);
                Button btnAddToList = view.findViewById(R.id.btnAddToList);

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

                btnAddToList.setOnClickListener(v2 -> {
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("Watchlist");
                    String sTitleTrim = result.getTitle().replace(".","").replace("#", "").replace("[","").replace("]", "");
                    result.setTitle(sTitleTrim);
                    result.setUser(user);
                    String sTrimUser = user.replace(".","").replace("#", "").replace("[","").replace("]", "");
                    reference.child(result.getId().toString()+"_"+sTrimUser).setValue(result);
                    String addPage = String.format("You have added "+result.getTitle()+" to your list!");
                    Toast.makeText(v2.getContext(), ""+addPage, Toast.LENGTH_SHORT).show();
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
        int recordsNum = totalPages * 20;
        int moviewNum = position + 1;
        if (position == 0) {
            return ITEM;
        } else if (position == movieResults.size() - 1 && isLoadingAdded) {
            return LOADING;
        } else if (position == recordsNum - 1) {
            return NO_ITEM;
        } else {
            return ITEM;
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

    public void setVisibility(RecyclerView.ViewHolder holder){
        holder.itemView.setVisibility(View.GONE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
    }


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
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;
    }

    public Result getItem(int position) {
        return movieResults.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(movieResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    @Override
    public void retryPageLoad() {

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

        public MovieVH(View itemView) {
            super(itemView);

            mMovieTitle = itemView.findViewById(R.id.movie_title);
            mMovieDesc = itemView.findViewById(R.id.movie_desc);
            mYear = itemView.findViewById(R.id.movie_year);
            mPosterImg = itemView.findViewById(R.id.movie_poster);
            mProgress = itemView.findViewById(R.id.movie_progress);
        }
    }

    protected class NoItemVH extends RecyclerView.ViewHolder{
        private TextView txtNoResults;
        private TextView mMovieTitle;
        private TextView mMovieDesc;
        private TextView mYear; // displays "year | language"
        private ImageView mPosterImg;
        private ProgressBar mProgressBar;
        private LinearLayout mLinearLayout;
        private CardView mCardView;

        public NoItemVH(@NonNull View itemView) {
            super(itemView);

            txtNoResults = itemView.findViewById(R.id.noResults);
            mMovieTitle = itemView.findViewById(R.id.movie_title);
            mMovieDesc = itemView.findViewById(R.id.movie_desc);
            mYear = itemView.findViewById(R.id.movie_year);
            mPosterImg = itemView.findViewById(R.id.movie_poster);
            mProgressBar = itemView.findViewById(R.id.movie_progress);
            mLinearLayout = itemView.findViewById(R.id.movie_linear_layout);
            mCardView = itemView.findViewById(R.id.movie_card_view);
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
