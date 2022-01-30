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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.durobelacic.moviemania.Fragments.ResultsFragment;
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

public class FilterMoviePaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int NoItem = 2;

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

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private List<Result> filteredMovies = new ArrayList<>();

    private Integer currentPage2;

    public FilterMoviePaginationAdapter(Context context, RequestManager glide, BottomSheetDialog bottomSheetDialog, View view, String user, String keyword,
                                        String year, String lang, String genre) {
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
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Result result = movieResults.get(position); // Movie
        switch (getItemViewType(position)) {
            case ITEM:
                if(result.getTitle().toLowerCase().contains(keyword) && formatYearLabelYear(result).contains(year) && result.getOriginalLanguage().contains(lang)
                        && result.getGenreIds().toString().contains(genre))
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
                Button btnAddToList = view.findViewById(R.id.btnAddToList);

                movieDesc.setMovementMethod(new ScrollingMovementMethod());

                movieTitle.setText(result.getTitle());
                movieYear.setText(formatYearLabelYear(result));
                movieLang.setText(result.getOriginalLanguage().toUpperCase());
                movieDesc.setText(result.getOverview());

                RoundedImageView roundedImageView = view.findViewById(R.id.mPosterImg);
                glide
                        .load(BASE_URL_IMG + result.getPosterPath())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(roundedImageView);

                btnAddToList.setOnClickListener(v2 -> {
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("Watchlist");
                    result.setUser(user);
                    reference.child(result.getTitle()).setValue(result);
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
            return (position == movieResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
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

//    private DrawableRequestBuilder<String> loadImage(@NonNull String posterPath) {
//        return Glide
//                .with(context)
//                .load(BASE_URL_IMG + posterPath)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
//                .centerCrop()
//                .crossFade();

//    }
   /* private GlideRequest<Drawable> loadImage(@NonNull String posterPath) {
        return GlideApp
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .centerCrop();
    }*/

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

    public void getCurrentPage(int currentPage){
        currentPage2 = currentPage;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

//        int position = movieResults.size() - 1;
//        Result result = getItem(position);
//
//        if (result != null) {
//            movieResults.remove(position);
//            notifyItemRemoved(position);
//        }
    }

    public Result getItem(int position) {
        return movieResults.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
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
}
