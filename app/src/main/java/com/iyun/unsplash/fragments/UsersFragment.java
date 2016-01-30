package com.iyun.unsplash.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.iyun.unsplash.models.User;
import com.iyun.unsplash.models.UserList;
import com.iyun.unsplash.network.UnsplashApi;
import com.iyun.unsplash.views.adapters.UserAdapter;
import com.iyun.unsplash.OnItemClickListener;
import com.iyun.unsplash.R;
import com.iyun.unsplash.activities.SingleUserActivity;
import com.iyun.unsplash.models.ImageList;
import com.sch.rfview.AnimRFRecyclerView;

import java.util.ArrayList;

import retrofit.RetrofitError;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tr.xip.errorview.ErrorView;
import tr.xip.errorview.RetryListener;

public class UsersFragment extends Fragment {

    public static SparseArray<Bitmap> photoCache = new SparseArray<>(1);

    private UnsplashApi mApi = new UnsplashApi();

    private UserAdapter mUserAdapter;
    private ArrayList<User> mUsers;
    private ArrayList<User> mCurrentUsers;
    private AnimRFRecyclerView mImageRecycler;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    Log.d("qiqi","update ");
                    updateAdapter(mUsers);
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

//        if (UsersFragment.this.getActivity() instanceof MainActivity) {
//            ((MainActivity) UsersFragment.this.getActivity()).setOnFilterChangedListener(new MainActivity.OnFilterChangedListener() {
//                @Override
//                public void onFilterChanged(int filter) {
//                    if (mUsers != null) {
//                        if (filter == MainActivity.Category.ALL.id) {
//                            showAll();
//                        } else if (filter == MainActivity.Category.FEATURED.id) {
//                            showFeatured();
//                        } else if (filter == MainActivity.Category.LOVED.id) {
//                            //TODO
//                        } else {
//                            showCategory(filter);
//                        }
//                    }
//                }
//            });
//        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_users, container, false);
        mImageRecycler = (AnimRFRecyclerView) rootView.findViewById(R.id.fragment_last_images_recycler);
        mImageRecycler.setColor(R.color.back_white, R.color.back_white);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_images_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_images_error_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mUserAdapter = new UserAdapter();
        mUserAdapter.setOnItemClickListener(recyclerRowClickListener);
        mImageRecycler.setAdapter(mUserAdapter);

        showAll();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showAll() {
//        if (mUsers != null) {
//            updateAdapter(mUsers);
//        } else {
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);

            // Load images from API
            mApi.fetchUsers().cache().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
//        }
    }

//    private void showFeatured() {
//        updateAdapter(mApi.filterFeatured(mImages));
//    }
//
//    private void showCategory(int category) {
//        updateAdapter(mApi.filterCategory(mImages, category));
//    }

    private void startToGetPics(){
        for (int i = 0; i < mUsers.size(); i++){
            User user = mUsers.get(i);
            mApi.fetchUserImages(user.getUid()).cache().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new PicObserver(i));
        }
    }
    class PicObserver implements Observer<ImageList>{
        int position = 0;
        public PicObserver(int position) {
            Log.d("qiqi", "set position:" + position);
            this.position = position;
        }

        @Override
        public void onNext(ImageList imageList) {
            Log.d("qiqi", "success position:" + position + " count:" + imageList.getData().size());
            if(imageList.getData().size() >=1){
                mUsers.get(position).setImage1(imageList.getData().get(0).getThumbnail());
                mUsers.get(position).setImage1_standard(imageList.getData().get(0).getStandard_resolution());
            }
            if(imageList.getData().size() >=2){
                mUsers.get(position).setImage2(imageList.getData().get(1).getThumbnail());
                mUsers.get(position).setImage2_standard(imageList.getData().get(1).getStandard_resolution());
            }
            ArrayList<User> nUsers = mUsers;
            UserAdapter newAdapter = new UserAdapter(mUsers);
            newAdapter.setOnItemClickListener(recyclerRowClickListener);
            mImageRecycler.setAdapter(newAdapter);
        }

        @Override
        public void onError(Throwable e) {
            Log.d("qiqi", "error position:" + position + " e:" + e.toString());
        }

        @Override
        public void onCompleted() {
            Log.d("qiqi", "completed position:" + position);
        }
    }
    private Observer<UserList> observer = new Observer<UserList>() {
        @Override
        public void onNext(final UserList users) {
            mUsers = users.getData();
            Log.d("qiqi", "start to get pics");
            ArrayList<User> nUsers = new ArrayList<User>();
            for (User user : mUsers){
                if(user.getWorks() > 0){
                    nUsers.add(user);
                }
            }
            mUsers = nUsers;
            startToGetPics();
            updateAdapter(mUsers);
//            handler.sendEmptyMessageDelayed(0, 5000);
//            if (UsersFragment.this.getActivity() instanceof MainActivity) {
//                ((MainActivity) UsersFragment.this.getActivity()).setCategoryCount(images);
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
            Log.d("qiqi", "error:" + error.toString());
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
            Log.d("qiqi", "onclicked:" + position);
            Intent intent = new Intent(getActivity(), SingleUserActivity.class);
            intent.putExtra("uid", mUsers.get(position - 1).getUid());
            getActivity().startActivity(intent);
//            User selectedImage = mCurrentUsers.get(position);
//
//            Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
//            detailIntent.putExtra("position", position);
//            detailIntent.putExtra("selected_image", selectedImage);
//
//            if (selectedImage.getSwatch() != null) {
//                detailIntent.putExtra("swatch_title_text_color", selectedImage.getSwatch().getTitleTextColor());
//                detailIntent.putExtra("swatch_rgb", selectedImage.getSwatch().getRgb());
//            }
//
//            ImageView coverImage = (ImageView) v.findViewById(R.id.item_image_img);
//            if (coverImage == null) {
//                coverImage = (ImageView) ((View) v.getParent()).findViewById(R.id.item_image_img);
//            }
//
//            if (Build.VERSION.SDK_INT >= 21) {
//                if (coverImage.getParent() != null) {
//                    ((ViewGroup) coverImage.getParent()).setTransitionGroup(false);
//                }
//            }
//
//            if (coverImage != null && coverImage.getDrawable() != null) {
//                Bitmap bitmap = ((BitmapDrawable) coverImage.getDrawable()).getBitmap(); //ew
//                if (bitmap != null && !bitmap.isRecycled()) {
//                    photoCache.put(position, bitmap);
//
//                    // Setup the transition to the detail activity
//                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), coverImage, "cover");
//
//                    startActivity(detailIntent, options.toBundle());
//                }
//            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
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
     * @param users
     */
    private void updateAdapter(ArrayList<User> users) {
        mCurrentUsers = users;
        mUserAdapter.updateData(mCurrentUsers);
//        mImageRecycler.scrollToPosition(0);
        /*
        mImageAdapter = new ImageAdapter(images);
        mImageAdapter.setOnItemClickListener(recyclerRowClickListener);
        mImageRecycler.setAdapter(mImageAdapter);
        */
    }
}
