package com.mikepenz.unsplash.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.unsplash.OnItemClickListener;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.models.User;
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

        usersViewHolder.userDetail.setText(String.format(mContext.getResources().getString(R.string.user_detail),"深圳", 11, 111));
        //imagesViewHolder.imageView.setDrawingCacheEnabled(true);
        usersViewHolder.imageView1.setImageBitmap(null);

        //reset colors so we prevent crazy flashes :D
        usersViewHolder.userAuthor.setTextColor(mDefaultTextColor);
        usersViewHolder.userDetail.setTextColor(mDefaultTextColor);

        //cancel any loading images on this view
        Picasso.with(mContext).cancelRequest(usersViewHolder.imageView1);
        //load the image
//        Picasso.with(mContext).load(mUsers.get(position).getThumbnail()).transform(PaletteTransformation.instance()).into(usersViewHolder.imageView, new Callback.EmptyCallback() {
//            @Override
//            public void onSuccess() {
//                Bitmap bitmap = ((BitmapDrawable) usersViewHolder.imageView.getDrawable()).getBitmap(); // Ew!
//
//                if (bitmap != null && !bitmap.isRecycled()) {
//                    Palette palette = PaletteTransformation.getPalette(bitmap);
//
//                    if (palette != null) {
//                        Palette.Swatch s = palette.getVibrantSwatch();
//                        if (s == null) {
//                            s = palette.getDarkVibrantSwatch();
//                        }
//                        if (s == null) {
//                            s = palette.getLightVibrantSwatch();
//                        }
//                        if (s == null) {
//                            s = palette.getMutedSwatch();
//                        }
//
//                        if (s != null && position >= 0 && position < mImages.size()) {
//                            if (mImages.get(position) != null) {
//                                mImages.get(position).setSwatch(s);
//                            }
//
//                            usersViewHolder.imageAuthor.setTextColor(s.getTitleTextColor());
//                            usersViewHolder.imageDate.setTextColor(s.getTitleTextColor());
//                            Utils.animateViewColor(usersViewHolder.imageTextContainer, mDefaultBackgroundColor, s.getRgb());
//                        }
//                    }
//                }
//
//                // just delete the reference again.
//                bitmap = null;
//
//                if (Build.VERSION.SDK_INT >= 21) {
//                    usersViewHolder.imageView.setTransitionName("cover" + position);
//                }
//                usersViewHolder.imageTextContainer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onItemClickListener.onClick(v, position);
//                    }
//                });
//            }
//
//        });


        //calculate height of the list-item so we don't have jumps in the view
        DisplayMetrics displaymetrics = mContext.getResources().getDisplayMetrics();
        //image.width .... image.height
        //device.width ... device
        int finalHeight = (int) (displaymetrics.widthPixels / 4);
        usersViewHolder.imageView1.setMaxHeight(finalHeight);
        usersViewHolder.imageView1.setMaxHeight(finalHeight);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}

class UsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected final ImageView imageView1;
    protected final ImageView imageView2;
    protected final TextView userAuthor;
    protected final TextView userDetail;
    private final OnItemClickListener onItemClickListener;

    public UsersViewHolder(View itemView, OnItemClickListener onItemClickListener) {

        super(itemView);
        this.onItemClickListener = onItemClickListener;

        imageView1 = (ImageView) itemView.findViewById(R.id.item_image_img1);
        imageView2 = (ImageView) itemView.findViewById(R.id.item_image_img2);
        userAuthor = (TextView) itemView.findViewById(R.id.item_user_author);
        userDetail = (TextView) itemView.findViewById(R.id.item_user_detail);

//        imageView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }
}

