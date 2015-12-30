package com.mikepenz.unsplash.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.unsplash.OnItemClickListener;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.activities.DetailActivity;
import com.mikepenz.unsplash.activities.MainActivity;
import com.mikepenz.unsplash.activities.SingleUserActivity;
import com.mikepenz.unsplash.activities.UserActivity;
import com.mikepenz.unsplash.models.Image;
import com.mikepenz.unsplash.models.ImageList;
import com.mikepenz.unsplash.models.User;
import com.mikepenz.unsplash.models.UserList;
import com.mikepenz.unsplash.network.UnsplashApi;
import com.mikepenz.unsplash.views.adapters.ImageAdapter;
import com.sch.rfview.AnimRFRecyclerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tr.xip.errorview.ErrorView;
import tr.xip.errorview.RetryListener;

public class SingleUserFragment extends Fragment {

    public static SparseArray<Bitmap> photoCache = new SparseArray<>(1);

    private UnsplashApi mApi = new UnsplashApi();

    private ImageAdapter mImageAdapter;
    private ArrayList<Image> mImages;
    private ArrayList<Image> mCurrentImages;
    private AnimRFRecyclerView mImageRecycler;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private ImageView mUserProfile;
    private TextView mUserDetail;
    private int curUid;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        if (SingleUserFragment.this.getActivity() instanceof MainActivity) {
            ((MainActivity) SingleUserFragment.this.getActivity()).setOnFilterChangedListener(new MainActivity.OnFilterChangedListener() {
                @Override
                public void onFilterChanged(int filter) {
                    if (mImages != null) {
                        if (filter == MainActivity.Category.ALL.id) {
                            showAll();
                        } else if (filter == MainActivity.Category.FEATURED.id) {
                            showFeatured();
                        } else if (filter == MainActivity.Category.LOVED.id) {
                            //TODO
                        } else {
                            showCategory(filter);
                        }
                    }
                }
            });
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        mImageRecycler = (AnimRFRecyclerView) rootView.findViewById(R.id.fragment_last_images_recycler);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_images_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_images_error_view);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.user_info_head_viewer, null);
        mUserProfile = (ImageView) headerView.findViewById(R.id.user_profile);
        mUserDetail = (TextView) headerView.findViewById(R.id.user_detail);
        mImageRecycler.addHeaderView(headerView);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mImageAdapter = new ImageAdapter();
        mImageAdapter.setOnItemClickListener(recyclerRowClickListener);
        mImageRecycler.setAdapter(mImageAdapter);
        curUid = ((SingleUserActivity)getActivity()).curUid;
        showAll();


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showAll() {
        if (mImages != null) {
            updateAdapter(mImages);
        } else {
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
            Log.d("qiqi", "singleUser curUid:" + curUid);
            // Load images from API
            mApi.fetchUserImages(curUid).cache().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
            mApi.fetchUser(curUid).cache().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userObserver);
        }
    }
    private Observer<UserList> userObserver = new Observer<UserList>() {
        @Override
        public void onNext(final UserList users) {
            ArrayList<User> mUsers = users.getData();

            Log.d("qiqi", "1:" + "http://www.iyun720.com/data/avatar/000/00/00/" + mUsers.get(0).getUid() + "_avatar_middle.jpg");
            Picasso.with(getActivity()).load("http://www.iyun720.com/data/avatar/000/00/00/"+ mUsers.get(0).getUid() +"_avatar_middle.jpg" ).into(mUserProfile);
            mUserDetail.setText(String.format(getActivity().getResources().getString(R.string.user_detail_single),mUsers.get(0).getWorks() , mUsers.get(0).getFollow(), mUsers.get(0).getFollowed()));
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(final Throwable error) {
        }
    };
    private void showFeatured() {
        updateAdapter(mApi.filterFeatured(mImages));
    }

    private void showCategory(int category) {
        updateAdapter(mApi.filterCategory(mImages, category));
    }

    private Observer<ImageList> observer = new Observer<ImageList>() {
        @Override
        public void onNext(final ImageList images) {
            mImages = images.getData();
            Log.d("qiqi", "singleUser pic:" + mImages.size());
            updateAdapter(mImages);

//            if (SingleUserFragment.this.getActivity() instanceof MainActivity) {
//                ((MainActivity) SingleUserFragment.this.getActivity()).setCategoryCount(images);
//            }
        }

        @Override
        public void onCompleted() {
            // Dismiss loading dialog
            mImagesProgress.setVisibility(View.GONE);
            mImageRecycler.setVisibility(View.VISIBLE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        @Override
        public void onError(final Throwable error) {
            if (error instanceof RetrofitError) {
                RetrofitError e = (RetrofitError) error;
                if (e.getKind() == RetrofitError.Kind.NETWORK) {
                    mImagesErrorView.setErrorTitle(R.string.error_network);
                    mImagesErrorView.setErrorSubtitle(R.string.error_network_subtitle);
                } else if (e.getKind() == RetrofitError.Kind.HTTP) {
                    mImagesErrorView.setErrorTitle(R.string.error_server);
                    mImagesErrorView.setErrorSubtitle(R.string.error_server_subtitle);
                } else {
                    mImagesErrorView.setErrorTitle(R.string.error_uncommon);
                    mImagesErrorView.setErrorSubtitle(R.string.error_uncommon_subtitle);
                }
            }

            mImagesProgress.setVisibility(View.GONE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.VISIBLE);

            mImagesErrorView.setOnRetryListener(new RetryListener() {
                @Override
                public void onRetry() {
                    showAll();
                }
            });
        }
    };

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {

            Image selectedImage = mCurrentImages.get(position);

            Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
            detailIntent.putExtra("position", position);
            detailIntent.putExtra("selected_image", selectedImage);

            if (selectedImage.getSwatch() != null) {
                detailIntent.putExtra("swatch_title_text_color", selectedImage.getSwatch().getTitleTextColor());
                detailIntent.putExtra("swatch_rgb", selectedImage.getSwatch().getRgb());
            }

            ImageView coverImage = (ImageView) v.findViewById(R.id.item_image_img);
            if (coverImage == null) {
                coverImage = (ImageView) ((View) v.getParent()).findViewById(R.id.item_image_img);
            }

            if (Build.VERSION.SDK_INT >= 21) {
                if (coverImage.getParent() != null) {
                    ((ViewGroup) coverImage.getParent()).setTransitionGroup(false);
                }
            }

            if (coverImage != null && coverImage.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) coverImage.getDrawable()).getBitmap(); //ew
                if (bitmap != null && !bitmap.isRecycled()) {
                    photoCache.put(position, bitmap);

                    // Setup the transition to the detail activity
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), coverImage, "cover");

                    startActivity(detailIntent, options.toBundle());
                }
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_shuffle) {
            if (mImages != null) {
                //we don't want to shuffle the original list
                ArrayList<Image> shuffled = new ArrayList<Image>(mImages);
                Collections.shuffle(shuffled);
                mImageAdapter.updateData(shuffled);
                updateAdapter(shuffled);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * a small helper class to update the adapter
     *
     * @param images
     */
    private void updateAdapter(ArrayList<Image> images) {
        mCurrentImages = images;
        mImageAdapter.updateData(mCurrentImages);
        mImageRecycler.scrollToPosition(0);
        /*
        mImageAdapter = new ImageAdapter(images);
        mImageAdapter.setOnItemClickListener(recyclerRowClickListener);
        mImageRecycler.setAdapter(mImageAdapter);
        */
    }
}
