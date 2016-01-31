package com.iyun.unsplash.network;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iyun.unsplash.models.Image;
import com.iyun.unsplash.models.UserList;
import com.iyun.unsplash.CustomApplication;
import com.iyun.unsplash.models.ImageList;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public class UnsplashApi {
    public static final String ENDPOINT = "http://t.iyun720.com:12343/";
    private final UnsplashService mWebService;

    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create(); //2015-01-18 15:48:56

    public UnsplashApi() {
        Cache cache = null;
        OkHttpClient okHttpClient = null;

        try {
            File cacheDir = new File(CustomApplication.getContext().getCacheDir().getPath(), "pictures.json");
            cache = new Cache(cacheDir, 10 * 1024 * 1024);
            okHttpClient = new OkHttpClient();
            okHttpClient.setCache(cache);
        } catch (Exception e) {
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Cache-Control", "public, max-age=" + 60 * 60 * 4);
                    }
                })
                .build();
        mWebService = restAdapter.create(UnsplashService.class);
    }


    public interface UnsplashService {
        @GET("/?action=19")
        Observable<ImageList> listImages(@Query("order") int action,@Query("begin") int begin ,@Query("number") int number ,@Query("type") int type);

        @GET("/?action=20")
        Observable<UserList> listUsers(@Query("order") int action,@Query("begin") int begin ,@Query("number") int number);

        @GET("/")
        Observable<ImageList> listUserImages(@Query("action") int action ,@Query("uid") int uid);

        @GET("/")
        Observable<UserList> listUser(@Query("action") int action ,@Query("uid") int uid);

    }

    public interface RandomUnsplashService {
        @GET("/random")
        Image random();
    }

    public Observable<ImageList> fetchImages(int order, int begin, int number, int type) {
        return mWebService.listImages(order, begin, number, type);
    }

    public Observable<UserList> fetchUsers(int order, int begin, int number) {
        return mWebService.listUsers(order, begin, number);
    }

    public Observable<ImageList> fetchUserImages(int uid) {
        return mWebService.listUserImages(7, uid);
    }

    public Observable<UserList> fetchUser(int uid) {
        return mWebService.listUser(5, uid);
    }
    //keep the filtered array so we can reuse it later :D
    private ArrayList<Image> featured = null;

    public ArrayList<Image> filterFeatured(ArrayList<Image> images) {
        if (featured == null) {
            ArrayList<Image> list = new ArrayList<Image>(images);
            for (Iterator<Image> it = list.iterator(); it.hasNext(); ) {
//                if (it.next().getFeatured() != 1) {
//                    it.remove();
//                }
            }
            featured = list;
        }
        return featured;
    }

    public static int countFeatured(ArrayList<Image> images) {
        int count = 0;
        for (Image image : images) {
//            if (image.getFeatured() == 1) {
//                count = count + 1;
//            }
        }
        return count;
    }

    public ArrayList<Image> filterCategory(ArrayList<Image> images, int filter) {
        ArrayList<Image> list = new ArrayList<Image>(images);
        for (Iterator<Image> it = list.iterator(); it.hasNext(); ) {
//            if ((it.next().getCategory() & filter) != filter) {
//                it.remove();
//            }
        }
        return list;
    }

    public static int countCategory(ArrayList<Image> images, int filter) {
        int count = 0;
        for (Image image : images) {
//            if ((image.getCategory() & filter) == filter) {
//                count = count + 1;
//            }
        }
        return count;
    }
}
