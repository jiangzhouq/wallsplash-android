package com.iyun.unsplash.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iyun.unsplash.OnItemClickListener;
import com.iyun.unsplash.R;
import com.iyun.unsplash.models.Image;
import com.iyun.unsplash.other.PaletteTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageAdapter extends RecyclerView.Adapter<ImagesViewHolder> {

    private Context mContext;

    private ArrayList<Image> mImages;

    private int mScreenWidth;

    private int mDefaultTextColor;
    private int mDefaultBackgroundColor;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ImageAdapter() {
    }

    public ImageAdapter(ArrayList<Image> images) {
        this.mImages = images;
    }

    public void updateData(ArrayList<Image> images) {
        this.mImages = images;
//        notifyDataSetChanged();
    }

    @Override
    public ImagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        View rowView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_image, viewGroup, false);

        //set the mContext
        this.mContext = viewGroup.getContext();

        //get the colors

        //get the screenWidth :D optimize everything :D
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;

        return new ImagesViewHolder(rowView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final ImagesViewHolder imagesViewHolder, final int position) {

        final Image currentImage = mImages.get(position);
        imagesViewHolder.imageSummary.setText(currentImage.getTitle());

        imagesViewHolder.imageAuthor.setText(currentImage.getUsername());
        //imagesViewHolder.imageView.setDrawingCacheEnabled(true);
        imagesViewHolder.imageView.setImageBitmap(null);


        //reset colors so we prevent crazy flashes :D
//        imagesViewHolder.imageAuthor.setTextColor(mDefaultTextColor);
//        imagesViewHolder.imageDate.setTextColor(mDefaultTextColor);
//        imagesViewHolder.imageTextContainer.setBackgroundColor(mDefaultBackgroundColor);
        if(mImages.get(position).getStandard_resolution().contains(".mp4")){
            imagesViewHolder.playView.setVisibility(View.VISIBLE);
        }else{
            imagesViewHolder.playView.setVisibility(View.GONE);
        }

        //cancel any loading images on this view
        Picasso.with(mContext).cancelRequest(imagesViewHolder.userPRofile);
        //load the image
        Picasso.with(mContext).load("http://www.iyun720.com/data/avatar/000/00/00/"+ currentImage.getUid() +"_avatar_middle.jpg" ).transform(PaletteTransformation.instance()).into(imagesViewHolder.userPRofile, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = null;
                if((BitmapDrawable) imagesViewHolder.userPRofile.getDrawable() != null){
                    bitmap = ((BitmapDrawable) imagesViewHolder.userPRofile.getDrawable()).getBitmap(); // Ew!

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

                        if (s != null && position >= 0 && position < mImages.size()) {
                            if (mImages.get(position) != null) {
                                mImages.get(position).setSwatch(s);
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
                    imagesViewHolder.userPRofile.setTransitionName("cover" + position);
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
                imagesViewHolder.userPRofile.setImageResource(R.drawable.ic_launcher);
            }
        });

        //cancel any loading images on this view
        Picasso.with(mContext).cancelRequest(imagesViewHolder.imageView);
        //load the image
        Picasso.with(mContext).load(mImages.get(position).getThumbnail()).transform(PaletteTransformation.instance()).into(imagesViewHolder.imageView, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) imagesViewHolder.imageView.getDrawable()).getBitmap(); // Ew!

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

                        if (s != null && position >= 0 && position < mImages.size()) {
                            if (mImages.get(position) != null) {
                                mImages.get(position).setSwatch(s);
                            }

//                            imagesViewHolder.imageAuthor.setTextColor(s.getTitleTextColor());
//                            imagesViewHolder.imageDate.setTextColor(s.getTitleTextColor());
//                            Utils.animateViewColor(imagesViewHolder.imageTextContainer, mDefaultBackgroundColor, s.getRgb());
                        }
                    }
                }

                // just delete the reference again.
                bitmap = null;

                if (Build.VERSION.SDK_INT >= 21) {
                    imagesViewHolder.imageView.setTransitionName("cover" + position);
                }
//                imagesViewHolder.imageTextContainer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onItemClickListener.onClick(v, position);
//                    }
//                });
            }

        });


        //calculate height of the list-item so we don't have jumps in the view
        DisplayMetrics displaymetrics = mContext.getResources().getDisplayMetrics();
        //image.width .... image.height
        //device.width ... device
        int finalHeight = (int) (displaymetrics.widthPixels / 3);
        imagesViewHolder.imageContainer.setMinimumHeight(finalHeight);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }
}

class ImagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected final LinearLayout imageTextContainer;
    protected final RelativeLayout imageContainer;

    protected final ImageView imageView;
    protected final ImageView playView;
    protected final TextView imageAuthor;
    protected final TextView imageSummary;
    private final OnItemClickListener onItemClickListener;
    protected final CircleImageView userPRofile;

    public ImagesViewHolder(View itemView, OnItemClickListener onItemClickListener) {

        super(itemView);
        this.onItemClickListener = onItemClickListener;

        imageTextContainer = (LinearLayout) itemView.findViewById(R.id.item_image_text_container);
        imageView = (ImageView) itemView.findViewById(R.id.item_image_img);
        imageAuthor = (TextView) itemView.findViewById(R.id.item_image_author);
        imageSummary = (TextView) itemView.findViewById(R.id.item_image_summary);
        playView = (ImageView) itemView.findViewById(R.id.play);
        imageContainer = (RelativeLayout) itemView.findViewById(R.id.image_container);
        userPRofile = (CircleImageView) itemView.findViewById(R.id.user_profile);
        imageView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }
}

