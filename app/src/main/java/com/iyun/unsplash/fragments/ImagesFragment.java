package com.iyun.unsplash.fragments;


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

import com.iyun.unsplash.activities.DetailActivity;
import com.iyun.unsplash.models.Image;
import com.iyun.unsplash.models.User;
import com.iyun.unsplash.models.UserList;
import com.iyun.unsplash.OnItemClickListener;
import com.iyun.unsplash.R;
import com.iyun.unsplash.activities.MainActivity;
import com.iyun.unsplash.activities.UserActivity;
import com.iyun.unsplash.models.ImageList;
import com.iyun.unsplash.network.UnsplashApi;
import com.iyun.unsplash.views.adapters.ImageAdapter;
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

public class ImagesFragment extends Fragment {

    public static SparseArray<Bitmap> photoCache = new SparseArray<>(1);

    private UnsplashApi mApi = new UnsplashApi();

    private ImageAdapter mImageAdapter;
    private ArrayList<Image> mImages;
    private ArrayList<Image> mCurrentImages;
    private AnimRFRecyclerView mImageRecycler;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private CircleImageView mUserProfile1;
    private CircleImageView mUserProfile2;
    private CircleImageView mUserProfile3;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        if (ImagesFragment.this.getActivity() instanceof MainActivity) {
            ((MainActivity) ImagesFragment.this.getActivity()).setOnFilterChangedListener(new MainActivity.OnFilterChangedListener() {
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

        View rootView = inflater.inflate(R.layout.fragment_images, container, false);
        mImageRecycler = (AnimRFRecyclerView) rootView.findViewById(R.id.fragment_last_images_recycler);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_images_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_images_error_view);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.findusers_head_viewer, null);
        mUserProfile1 = (CircleImageView) headerView.findViewById(R.id.user_profile_1);
        mUserProfile2 = (CircleImageView) headerView.findViewById(R.id.user_profile_2);
        mUserProfile3 = (CircleImageView) headerView.findViewById(R.id.user_profile_3);
        mImageRecycler.addHeaderView(headerView);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).startActivity(new Intent(getActivity(), UserActivity.class));
            }
        });
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

            // Load images from API
            mApi.fetchImages().cache().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
            mApi.fetchUsers().cache().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(topUserobserver);
        }
    }
    private Observer<UserList> topUserobserver = new Observer<UserList>() {
        @Override
        public void onNext(final UserList users) {
            ArrayList<User> mUsers = users.getData();

            ArrayList<User> nUsers = new ArrayList<User>();
            for (User user : mUsers){
                if(user.getWorks() > 0){
                    nUsers.add(user);
                }
            }
            mUsers = nUsers;
            Log.d("qiqi", "1:" + "http://www.iyun720.com/data/avatar/000/00/00/" + mUsers.get(0).getUid() + "_avatar_middle.jpg");
            Picasso.with(getActivity()).load("http://www.iyun720.com/data/avatar/000/00/00/"+ mUsers.get(0).getUid() +"_avatar_middle.jpg" ).into(mUserProfile1);
            Picasso.with(getActivity()).load("http://www.iyun720.com/data/avatar/000/00/00/"+ mUsers.get(1).getUid() +"_avatar_middle.jpg" ).into(mUserProfile2);
            Picasso.with(getActivity()).load("http://www.iyun720.com/data/avatar/000/00/00/"+ mUsers.get(2).getUid() +"_avatar_middle.jpg" ).into(mUserProfile3);
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
            updateAdapter(mImages);

            if (ImagesFragment.this.getActivity() instanceof MainActivity) {
                ((MainActivity) ImagesFragment.this.getActivity()).setCategoryCount(images);
            }
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

            Log.d("qiqi", "position:" + position +
                    " adapter.getcount:" + mImageAdapter.getItemCount() +
                            " mCurrentImages.size:" + mCurrentImages.size() +
                            " mImages.size:" + mImages.size());
            Image selectedImage = mCurrentImages.get(position -2);

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

//        if (id == R.id.action_shuffle) {
//            if (mImages != null) {
//                //we don't want to shuffle the original list
//                ArrayList<Image> shuffled = new ArrayList<Image>(mImages);
//                Collections.shuffle(shuffled);
//                mImageAdapter.updateData(shuffled);
//                updateAdapter(shuffled);
//            }
//        }
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
