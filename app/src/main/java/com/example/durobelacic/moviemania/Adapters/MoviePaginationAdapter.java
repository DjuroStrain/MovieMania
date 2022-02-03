package com.example.durobelacic.moviemania.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.durobelacic.moviemania.Fragments.MovieDialogFragment;
import com.example.durobelacic.moviemania.Models.Result;
import com.example.durobelacic.moviemania.MovieManiaActivity;
import com.example.durobelacic.moviemania.R;
import com.example.durobelacic.moviemania.Utils.MoviePaginationAdapterCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.durobelacic.moviemania.Utils.PageViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;

public class MoviePaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int TOP = 2;

    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w200";

    private List<Result> movieResults;

    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private MoviePaginationAdapterCallback mCallback;

    private String errorMsg;

    private RequestManager glide;

    private BottomSheetDialog bottomSheetDialog;
    private View view;

    String user;

    FirebaseDatabase database;
    DatabaseReference reference;

    public MoviePaginationAdapter(Context context, RequestManager glide, BottomSheetDialog bottomSheetDialog, View view, String user) {
        this.context = context;
        this.mCallback = (MoviePaginationAdapterCallback) context;
        movieResults = new ArrayList<>();
        this.glide = glide;
        this.bottomSheetDialog = bottomSheetDialog;
        this.view = view;
        this.user = user;
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
            case TOP:
                View viewTOP = inflater.inflate(R.layout.item_top, parent, false);
                viewHolder = new TopVH(viewTOP);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Result result = movieResults.get(position); // Movie
        switch (getItemViewType(position)) {

            case TOP:
                final TopVH TopVH = (TopVH) holder;
                TopVH.mMovieTitle.setText(result.getTitle());
                TopVH.mYear.setText(formatYearLabel(result));
                TopVH.mMovieDesc.setText(result.getOverview());


                glide
                        .load(BASE_URL_IMG + result.getBackdropPath())
                        .into(TopVH.mPosterImg);
                break;

            case ITEM:

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
                    String sTitleTrim = result.getTitle().replace(".","").replace("#", "").replace("[","").replace("]", "");
                    result.setTitle(sTitleTrim);
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
        if (position == 0) {
            return TOP;
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
    private String formatYearLabel(Result result) {
        return result.getReleaseDate().substring(0, 4)  // samo godina
                + " | "
                + result.getOriginalLanguage().toUpperCase();
    }

    private String formatYearLabelYear(Result result) {
        return result.getReleaseDate().substring(0, 4);  // samo godina
    }


    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Result r) {
        movieResults.add(r);
        notifyItemInserted(movieResults.size() - 1);
    }

    public void addAll(List<Result> moveResults) {
        for (Result result : moveResults) {
            add(result);
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
    * Glavni film
    * */
    protected class TopVH extends RecyclerView.ViewHolder {
        private TextView mMovieTitle;
        private TextView mMovieDesc;
        private TextView mYear; //jezik + godina
        private ImageView mPosterImg;

        public TopVH(View itemView) {
            super(itemView);

            mMovieTitle = itemView.findViewById(R.id.movie_title);
            mMovieDesc = itemView.findViewById(R.id.movie_desc);
            mYear = itemView.findViewById(R.id.movie_year);
            mPosterImg = itemView.findViewById(R.id.movie_poster);
        }
    }

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
