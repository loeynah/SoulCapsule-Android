package com.finalwork.soulcapsule.util;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.finalwork.soulcapsule.R;

/**
 * Glide 图片加载封装。
 */
public final class ImageLoaderHelper {

    private ImageLoaderHelper() {
    }

    public static void loadRemote(Context context, ImageView imageView,
                                  @Nullable String imageUrl, int cornerRadiusDp) {
        String resolved = ImageUrlHelper.resolve(imageUrl);
        if (resolved == null) {
            imageView.setImageDrawable(null);
            return;
        }
        int radiusPx = (int) (cornerRadiusDp * context.getResources().getDisplayMetrics().density);
        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                .placeholder(R.drawable.shape_footprint_image)
                .error(R.drawable.shape_footprint_image);
        Glide.with(context)
                .load(resolved)
                .apply(options)
                .into(imageView);
    }

    public static void loadLocal(Context context, ImageView imageView, Uri uri, int cornerRadiusDp) {
        int radiusPx = (int) (cornerRadiusDp * context.getResources().getDisplayMetrics().density);
        RequestOptions options = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                .placeholder(R.drawable.shape_footprint_image)
                .error(R.drawable.shape_footprint_image);
        Glide.with(context)
                .load(uri)
                .apply(options)
                .into(imageView);
    }
}
