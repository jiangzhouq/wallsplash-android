package com.mikepenz.unsplash.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.unsplash.OnItemClickListener;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.models.User;
import com.mikepenz.unsplash.other.PaletteTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UsersViewHolder> {

    private Context mContext;

    private ArrayList<User> mUsers;

    private int mScreenWidth;

    private int mDefaultTextColor;
    private int mDefaultBackgroundColor;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public UserAdapter() {
    }

    public UserAdapter(ArrayList<User> users) {
        this.mUsers = users;
    }

    public void updateData(ArrayList<User> users) {
        Log.d("qiqi", "UserAdapter : updateData");
        this.mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public UsersViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        View rowView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_user, viewGroup, false);

        //set the mContext
        this.mContext = viewGroup.getContext();

        //get the colors
        mDefaultTextColor = mContext.getResources().getColor(R.color.text_without_palette);
        mDefaultBackgroundColor = mContext.getResources().getColor(R.color.image_without_palette);

        //get the screenWidth :D optimize everything :D
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;

        return new UsersViewHolder(rowView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final UsersViewHolder usersViewHolder, final int position) {

        final User currentUser = mUsers.get(position);
        usersViewHolder.userAuthor.setText(currentUser.getUsername());

        usersViewHolder.userDetail.setText(String.format(mContext.getResources().getString(R.string.user_detail), "", currentUser.getWorks(), currentUser.getPopular()));
        //imagesViewHolder.imageView.setDrawingCacheEnabled(true);
        usersViewHolder.imageView1.setImageBitmap(null);

        //reset colors so we prevent crazy flashes :D
//        usersViewHolder.userAuthor.setTextColor(mDefaultTextColor);
//        usersViewHolder.userDetail.setTextColor(mDefaultTextColor);

        //cancel any loading images on this view
        Picasso.with(mContext).cancelRequest(usersViewHolder.imageView1);
        //load the image
        Picasso.with(mContext).load(mUsers.get(position).getImage1()).transform(PaletteTransformation.instance()).into(usersViewHolder.imageView1, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) usersViewHolder.imageView1.getDrawable()).getBitmap(); // Ew!

                if (bitmap != null && !bitmap.isRecycled()) {
                    Palette palette = PaletteTransformation.getPalette(bitmap);

                    if (palette != null) {
                        Palette.Swatch s = palette.getVibrantSwatch();
                        if (s == null) {
                            s = palette.getDarkVibrantSwatch();
                        }
                        if (s == null) {
                            s = palette.getLightVibrantSwatch();
                        }
                        if (s == null) {
                            s = palette.getMutedSwatch();
                        }

                        if (s != null && position >= 0 && position < mUsers.size()) {
                            if (mUsers.get(position) != null) {
                                mUsers.get(position).setSwatch(s);
                            }

//                            usersViewHolder.userAuthor.setTextColor(s.getTitleTextColor());
//                            usersViewHolder.userDetail.setTextColor(s.getTitleTextColor());
//                            Utils.animateViewColor(usersViewHolder.imageTextContainer, mDefaultBackgroundColor, s.getRgb());
                        }
                    }
                }

                // just delete the reference again.
                bitmap = null;

                if (Build.VERSION.SDK_INT >= 21) {
                    usersViewHolder.imageView1.setTransitionName("cover" + position);
                }
//                usersViewHolder.imageTextContainer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onItemClickListener.onClick(v, position);
//                    }
//                });
            }

        });
        //cancel any loading images on this view
        Picasso.with(mContext).cancelRequest(usersViewHolder.imageView2);
        //load the image
        Log.d("qiqi", "load user:" + mUsers.get(position).getUid() + " iamge:" + mUsers.get(position).getImage1());
        Picasso.with(mContext).load(mUsers.get(position).getImage2()).transform(PaletteTransformation.instance()).into(usersViewHolder.imageView2, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = null;
                if((BitmapDrawable) usersViewHolder.imageView1.getDrawable() != null){
                    bitmap = ((BitmapDrawable) usersViewHolder.imageView1.getDrawable()).getBitmap(); // Ew!

                }

                if (bitmap != null && !bitmap.isRecycled()) {
                    Palette palette = PaletteTransformation.getPalette(bitmap);

                    if (palette != null) {
                        Palette.Swatch s = palette.getVibrantSwatch();
                        if (s == null) {
                            s = palette.getDarkVibrantSwatch();
                        }
                        if (s == null) {
                            s = palette.getLightVibrantSwatch();
                        }
                        if (s == null) {
                            s = palette.getMutedSwatch();
                        }

                        if (s != null && position >= 0 && position < mUsers.size()) {
                            if (mUsers.get(position) != null) {
                                mUsers.get(position).setSwatch(s);
                            }

//                            usersViewHolder.userAuthor.setTextColor(s.getTitleTextColor());
//                            usersViewHolder.userDetail.setTextColor(s.getTitleTextColor());
//                            Utils.animateViewColor(usersViewHolder.imageTextContainer, mDefaultBackgroundColor, s.getRgb());
                        }
                    }
                }

                // just delete the reference again.
                bitmap = null;

                if (Build.VERSION.SDK_INT >= 21) {
                    usersViewHolder.imageView1.setTransitionName("cover" + position);
                }
//                usersViewHolder.imageTextContainer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onItemClickListener.onClick(v, position);
//                    }
//                });
            }

        });
        //cancel any loading images on this view
        Picasso.with(mContext).cancelRequest(usersViewHolder.user_profile);
        //load the image
        Picasso.with(mContext).load("http://www.iyun720.com/data/avatar/000/00/00/"+ mUsers.get(position).getUid() +"_avatar_middle.jpg" ).transform(PaletteTransformation.instance()).into(usersViewHolder.user_profile, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = null;
                if((BitmapDrawable) usersViewHolder.user_profile.getDrawable() != null){
                    bitmap = ((BitmapDrawable) usersViewHolder.user_profile.getDrawable()).getBitmap(); // Ew!

                }

                if (bitmap != null && !bitmap.isRecycled()) {
                    Palette palette = PaletteTransformation.getPalette(bitmap);

                    if (palette != null) {
                        Palette.Swatch s = palette.getVibrantSwatch();
                        if (s == null) {
                            s = palette.getDarkVibrantSwatch();
                        }
                        if (s == null) {
                            s = palette.getLightVibrantSwatch();
                        }
                        if (s == null) {
                            s = palette.getMutedSwatch();
                        }

                        if (s != null && position >= 0 && position < mUsers.size()) {
                            if (mUsers.get(position) != null) {
                                mUsers.get(position).setSwatch(s);
                            }

//                            usersViewHolder.userAuthor.setTextColor(s.getTitleTextColor());
//                            usersViewHolder.userDetail.setTextColor(s.getTitleTextColor());
//                            Utils.animateViewColor(usersViewHolder.imageTextContainer, mDefaultBackgroundColor, s.getRgb());
                        }
                    }
                }

                // just delete the reference again.
                bitmap = null;

                if (Build.VERSION.SDK_INT >= 21) {
                    usersViewHolder.imageView1.setTransitionName("cover" + position);
                }
//                usersViewHolder.imageTextContainer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onItemClickListener.onClick(v, position);
//                    }
//                });
            }

            @Override
            public void onError() {
                usersViewHolder.user_profile.setImageResource(R.drawable.ic_launcher);
            }
        });
        //calculate height of the list-item so we don't have jumps in the view
        DisplayMetrics displaymetrics = mContext.getResources().getDisplayMetrics();
        //image.width .... image.height
        //device.width ... device
        int finalHeight = (int) (displaymetrics.widthPixels / 6);
        usersViewHolder.imageView1.setMaxHeight(finalHeight);
        usersViewHolder.imageView1.setMaxHeight(finalHeight);
    }

    @Override
    public int getItemCount() {
        if(mUsers == null)
            return 0;
        return mUsers.size();
    }
}

class UsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected final ImageView imageView1;
    protected final ImageView imageView2;
    protected final ImageView user_profile;
    protected final TextView userAuthor;
    protected final TextView userDetail;
    private final OnItemClickListener onItemClickListener;
    private LinearLayout item;
    public UsersViewHolder(View itemView, OnItemClickListener onItemClickListener) {

        super(itemView);
        this.onItemClickListener = onItemClickListener;

        imageView1 = (ImageView) itemView.findViewById(R.id.item_image_img1);
        imageView2 = (ImageView) itemView.findViewById(R.id.item_image_img2);
        userAuthor = (TextView) itemView.findViewById(R.id.item_user_author);
        userDetail = (TextView) itemView.findViewById(R.id.item_user_detail);
        user_profile = (ImageView) itemView.findViewById(R.id.user_profile);
        item = (LinearLayout) itemView.findViewById(R.id.user_item);
        item.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }
}

