package com.movit.platform.framework.utils;import android.os.Build.VERSION_CODES;import java.io.ByteArrayInputStream;import java.io.ByteArrayOutputStream;import java.io.File;import java.io.FileOutputStream;import java.io.IOException;import java.lang.ref.SoftReference;import java.util.HashMap;import java.util.Map;import android.annotation.SuppressLint;import android.content.ContentResolver;import android.content.ContentUris;import android.content.Context;import android.content.Intent;import android.database.Cursor;import android.graphics.Bitmap;import android.graphics.Bitmap.Config;import android.graphics.BitmapFactory;import android.graphics.Canvas;import android.graphics.ColorMatrix;import android.graphics.ColorMatrixColorFilter;import android.graphics.Matrix;import android.graphics.Paint;import android.graphics.PixelFormat;import android.graphics.PorterDuff.Mode;import android.graphics.PorterDuffXfermode;import android.graphics.Rect;import android.graphics.RectF;import android.graphics.drawable.Drawable;import android.media.ExifInterface;import android.net.Uri;import android.os.Build;import android.os.Environment;import android.provider.DocumentsContract;import android.provider.MediaStore;import android.util.Log;public class PicUtils {    private static final String TAG = "PicUtils";    public static Map<Integer, SoftReference<Bitmap>> caches = new HashMap<Integer, SoftReference<Bitmap>>();    /**     * 专为Android4.4以后设计的从Uri获取文件绝对路径，以前的方法已不好使     * Try to return the absolute file path from the given Uri     *     * @param context     * @param data     * @return the file path or null     */    @SuppressLint("NewApi")    public static String getPicturePath(Intent data, Context context) {        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;        Uri uri = data.getData();        // DocumentProvider        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {            // ExternalStorageProvider            if (isExternalStorageDocument(uri)) {                final String docId = DocumentsContract.getDocumentId(uri);                final String[] split = docId.split(":");                final String type = split[0];                if ("primary".equalsIgnoreCase(type)) {                    return Environment.getExternalStorageDirectory() + "/" + split[1];                }                // TODO handle non-primary volumes            }            // DownloadsProvider            else if (isDownloadsDocument(uri)) {                final String id = DocumentsContract.getDocumentId(uri);                final Uri contentUri = ContentUris.withAppendedId(                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));                return getDataColumn(context, contentUri, null, null);            }            // MediaProvider            else if (isMediaDocument(uri)) {                final String docId = DocumentsContract.getDocumentId(uri);                final String[] split = docId.split(":");                final String type = split[0];                Uri contentUri = null;                if ("image".equals(type)) {                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;                } else if ("video".equals(type)) {                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;                } else if ("audio".equals(type)) {                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;                }                final String selection = "_id=?";                final String[] selectionArgs = new String[] { split[1] };                return getDataColumn(context, contentUri, selection, selectionArgs);            }        }        // MediaStore (and general)        else if ("content".equalsIgnoreCase(uri.getScheme())) {            return getDataColumn(context, uri, null, null);        }        // File        else if ("file".equalsIgnoreCase(uri.getScheme())) {            return uri.getPath();        }        return null;    }    /**     * Get the value of the data column for this Uri. This is useful for     * MediaStore Uris, and other file-based ContentProviders.     *     * @param context     *            The context.     * @param uri     *            The Uri to query.     * @param selection     *            (Optional) Filter used in the query.     * @param selectionArgs     *            (Optional) Selection arguments used in the query.     * @return The value of the _data column, which is typically a file path.     */    public static String getDataColumn(Context context, Uri uri, String selection,                                       String[] selectionArgs) {        Cursor cursor = null;        final String column = "_data";        final String[] projection = { column };        try {            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,                    null);            if (cursor != null && cursor.moveToFirst()) {                final int column_index = cursor.getColumnIndexOrThrow(column);                return cursor.getString(column_index);            }        } finally {            if (cursor != null)                cursor.close();        }        return null;    }    /**     * @param uri     *            The Uri to check.     * @return Whether the Uri authority is ExternalStorageProvider.     */    public static boolean isExternalStorageDocument(Uri uri) {        return "com.android.externalstorage.documents".equals(uri.getAuthority());    }    /**     * @param uri     *            The Uri to check.     * @return Whether the Uri authority is DownloadsProvider.     */    public static boolean isDownloadsDocument(Uri uri) {        return "com.android.providers.downloads.documents".equals(uri.getAuthority());    }    /**     * @param uri     *            The Uri to check.     * @return Whether the Uri authority is MediaProvider.     */    public static boolean isMediaDocument(Uri uri) {        return "com.android.providers.media.documents".equals(uri.getAuthority());    }//    /**//     * Android从相册中获取图片以及路径 并且存储到自己的目录中//     */////    public static String getPicturePath(Intent data, Context context) {////        // Intent intent = new Intent(Intent.ACTION_PICK, null);////        // intent.setDataAndType(////        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");////        // startActivityForResult(intent, 1);//////        // 插入系统相册 MediaStore.Images.Media.insertImage(getContentResolver(),////        // mBitmap, "title", "description");////        // 插入后要去扫描sd卡，不然打开相册会找不到，因为没有刷新////        // Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);////        // 路径的获取方法和下面的ContentResolver 方法一样 insertImage 返回的uri////        // Uri uri = Uri.fromFile(new File("/sdcard/image.jpg"));////        // intent.setData(uri);////        // mContext.sendBroadcast(intent);//////        Bitmap bm = null;////        String path = "";////        // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口////        ContentResolver resolver = context.getContentResolver();////        // 此处的用于判断接收的Activity是不是你想要的那个////        try {////            Uri originalUri = data.getData(); // 获得图片的uri////            // 防止oom////            bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); //////            // 显得到bitmap图片////            // 这里开始的第二部分，获取图片的路径：////            String[] proj = {MediaStore.Images.Media.DATA};////            // 好像是android多媒体数据库的封装接口，具体的看Android文档////            Cursor cursor = resolver.query(originalUri, proj, null, null, null);////            // 按我个人理解 这个是获得用户选择的图片的索引值////            int column_index = cursor////                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);////            // 将光标移至开头 ，这个很重要，不小心很容易引起越界////            cursor.moveToFirst();////            // 最后根据索引值获取图片路径////            path = cursor.getString(column_index);////            // if (bm != null) {////            // bm.recycle();////            // bm = null;////            // }////            return path;////        } catch (Exception e) {////            Log.e(TAG, e.toString());////            return path;////        }//////    }    public static String getPicSizeJson(String picPath) {        BitmapFactory.Options options = new BitmapFactory.Options();        /**         * 最关键在此，把options.inJustDecodeBounds = true;         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了         */        options.inJustDecodeBounds = true;        BitmapFactory.decodeFile(picPath, options);        // 此时返回的bitmap为null        /**         * options.outHeight为原始图片的高         */        Log.v("getPicSizeJson", "Bitmap Width == " + options.outWidth);        Log.v("getPicSizeJson", "Bitmap Height == " + options.outHeight);        int angle = getExifOrientation(picPath);        if (angle != 0) { // 如果照片出现了 旋转 那么 就更改旋转度数            if (angle == 90 || angle == 270) {                return "{" + options.outHeight + "," + options.outWidth + "}";            } else {                return "{" + options.outWidth + "," + options.outHeight + "}";            }        } else {            return "{" + options.outWidth + "," + options.outHeight + "}";        }    }    public static void scanImages(Context context, String filePath) {        try {            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);            Uri uri = Uri.fromFile(new File(filePath));            intent.setData(uri);            context.sendBroadcast(intent);        } catch (Exception e) {            // TODO Auto-generated catch block            e.printStackTrace();        }    }    /**     * 压缩图片并旋转后，保存为temp文件 此方法，虽然对文件压缩了，但是读取到bitmap的内存没有减少，像素比例没有变化 弃用     *     * @param srcPath     * @return     *     *         public static String compressImageFromFileAndRotaing(String     *         srcPath) { Bitmap bitmap = null; bitmap =     *         BitmapFactory.decodeFile(srcPath);     *     *         ByteArrayOutputStream baos = new ByteArrayOutputStream(); //     *         质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中 int angle =     *         getExifOrientation(srcPath); Log.e("angle", "angle = " + angle);     *         int options = 100; if (angle != 0) { options = 60; }     *         bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos); //     *         循环判断如果压缩后图片是否大于300kb,大于继续压缩 while (baos.toByteArray().length /     *         1024 > 500) { baos.reset();// 重置baos即清空baos options -= 20;//     *         每次都减少20 // 这里压缩options%，把压缩后的数据存放到baos中     *         bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos); }     *     *         try { baos.close(); } catch (IOException e) { // TODO     *         Auto-generated catch block e.printStackTrace(); }     *     *         Bitmap bt = null; if (angle != 0) { Bitmap zoomBitmap =     *         zoomImage(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);     *         bt = rotaingImageView(angle, zoomBitmap); } else { bt = bitmap; }     *         String path = saveBitmap(srcPath, bt, options);     *     *         return path; }     */    /**     * 此方法较之上面的 按比列读取bitmap 减少了对内存的使用     *     * @param srcPath     * @return     */    public static String getSmallImageFromFileAndRotaing(String srcPath) {        BitmapFactory.Options options = new BitmapFactory.Options();        options.inJustDecodeBounds = true;        BitmapFactory.decodeFile(srcPath, options);        options.inJustDecodeBounds = false;        options.inSampleSize = calculateInSampleSize(options, 480, 800);        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);        if (bitmap == null) {            return null;        }        int angle = getExifOrientation(srcPath);        if (angle != 0) {            bitmap = rotaingImageView(angle, bitmap);        }        // 有人说下面的压缩方法不管用 经实验 确实如此        // ByteArrayOutputStream baos = null;        // try {        // baos = new ByteArrayOutputStream();        // bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);        // } finally {        // try {        // if (baos != null)        // baos.close();        // } catch (IOException e) {        // e.printStackTrace();        // }        // }        String path = saveBitmap(srcPath, bitmap, 100);        return path;    }    private static int calculateInSampleSize(BitmapFactory.Options options,                                             int reqWidth, int reqHeight) {        // Raw height and width of image        final int height = options.outHeight;        final int width = options.outWidth;        int inSampleSize = 1;        if (height > reqHeight || width > reqWidth) {            // Calculate ratios of height and width to requested height and            // width            final int heightRatio = Math.round((float) height                    / (float) reqHeight);            final int widthRatio = Math.round((float) width / (float) reqWidth);            // Choose the smallest ratio as inSampleSize value, this will            // guarantee            // a final image with both dimensions larger than or equal to the            // requested height and width.            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;        }        return inSampleSize;    }    /**     * 拍照裁剪后，进行压缩保存。裁剪不压缩比例     *     * @param path     * @param image     * @param size     * @return     */    public static String compressImageAndSave(String path, Bitmap image,                                              int size) {        int options = 100;        ByteArrayOutputStream baos = new ByteArrayOutputStream();        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);        // 循环判断如果压缩后图片是否大于300kb,大于继续压缩        while (baos.toByteArray().length / 1024 > size) {            baos.reset();// 重置baos即清空baos            // 这里压缩options%，把压缩后的数据存放到baos中            image.compress(Bitmap.CompressFormat.JPEG, options, baos);            options -= 20;// 每次都减少20        }        return saveBitmap(path, image, options);    }    /***     * 图片的缩放方法     *     * @param bgimage   ：源图片资源     * @param newWidth  ：缩放后宽度     * @param newHeight ：缩放后高度     * @return     */    public static Bitmap zoomImage(Bitmap bgimage, int newWidth, int newHeight) {        // 获取这个图片的宽和高        int width = bgimage.getWidth();        int height = bgimage.getHeight();        // 创建操作图片用的matrix对象        Matrix matrix = new Matrix();        // 计算缩放率，新尺寸除原始尺寸        float scaleWidth = ((float) newWidth) / width;        float scaleHeight = ((float) newHeight) / height;        // 缩放图片动作        matrix.postScale(scaleWidth, scaleHeight);        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, width, height,                matrix, true);        return bitmap;    }    /**     * 将Drawable转化为Bitmap     */    public static Bitmap drawableToBitmap(Drawable drawable) {        int width = drawable.getIntrinsicWidth();        int height = drawable.getIntrinsicHeight();        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888                : Bitmap.Config.RGB_565);        Canvas canvas = new Canvas(bitmap);        drawable.setBounds(0, 0, width, height);        drawable.draw(canvas);        return bitmap;    }    /**     * 获得圆角图片的方法     */    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {        if (bitmap == null) {            return null;        }        if (roundPx == 0) {            return bitmap;        }        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),                bitmap.getHeight(), Config.ARGB_8888);        Canvas canvas = new Canvas(output);        final int color = 0xffbcbcbc;        final Paint paint = new Paint();        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());        final RectF rectF = new RectF(rect);        paint.setAntiAlias(true);        canvas.drawARGB(0, 0, 0, 0);        paint.setColor(color);        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));        canvas.drawBitmap(bitmap, rect, rect, paint);        return output;    }    /**     * 获得圆角图片的方法     */    public static Bitmap getRoundedCornerBitmap(Context context, int resource,                                                float roundPx) {        Bitmap bitmap = null;        if (caches.containsKey(resource)) {            if (caches.get(resource).get() == null) {                caches.remove(resource);                bitmap = BitmapFactory.decodeResource(context.getResources(),                        resource);            } else {                return caches.get(resource).get();            }        } else {            bitmap = BitmapFactory.decodeResource(context.getResources(),                    resource);        }        if (bitmap == null) {            return null;        }        if (roundPx == 0) {            return bitmap;        }        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),                bitmap.getHeight(), Config.ARGB_8888);        Canvas canvas = new Canvas(output);        final int color = 0xffbcbcbc;        final Paint paint = new Paint();        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());        final RectF rectF = new RectF(rect);        paint.setAntiAlias(true);        canvas.drawARGB(0, 0, 0, 0);        paint.setColor(color);        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));        canvas.drawBitmap(bitmap, rect, rect, paint);        caches.put(resource, new SoftReference<Bitmap>(output));        return output;    }    /**     * 得到 图片旋转 的角度     *     * @param filepath     * @return     */    public static int getExifOrientation(String filepath) {        int degree = 0;        ExifInterface exif = null;        try {            exif = new ExifInterface(filepath);        } catch (IOException ex) {        }        if (exif != null) {            int orientation = exif.getAttributeInt(                    ExifInterface.TAG_ORIENTATION, -1);            if (orientation != -1) {                switch (orientation) {                    case ExifInterface.ORIENTATION_ROTATE_90:                        degree = 90;                        break;                    case ExifInterface.ORIENTATION_ROTATE_180:                        degree = 180;                        break;                    case ExifInterface.ORIENTATION_ROTATE_270:                        degree = 270;                        break;                }            }        }        return degree;    }	/*	 * 旋转图片	 * @param angle	 * @param bitmap	 * @return Bitmap	 */    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {        // 旋转图片 动作        Matrix matrix = new Matrix();        matrix.postRotate(angle);        // 创建新的图片        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),                bitmap.getHeight(), matrix, true);        return bitmap;    }    /**     * 保存方法     */    public static String saveBitmap(String srcPath, Bitmap bitmap, int options) {        Log.e(TAG, "保存图片");        String newPath = getTempPicPath(srcPath);        File f = new File(newPath);        if (f.exists()) {            f.delete();        }        try {            FileOutputStream out = new FileOutputStream(f);            bitmap.compress(Bitmap.CompressFormat.JPEG, options, out);            out.flush();            out.close();            Log.e(TAG, f.getAbsolutePath());            return f.getAbsolutePath();        } catch (Exception e) {            e.printStackTrace();            return "";        } finally {            if (bitmap != null) {                if (Build.VERSION.SDK_INT < VERSION_CODES.O){                    bitmap.recycle();                }                bitmap = null;            }        }    }    //统一保存图片格式为jpg    public static String getTempPicPath(String path) {        int index = path.lastIndexOf(".");        String temp = path.substring(0, index);//        String postfix = path.substring(index);//        String newPath = temp + "_temp" + postfix;        String newPath = temp + "_temp" + ".jpg";        return newPath;    }    /**     * 图片转灰度     *     * @param bmSrc     * @return     */    public static Bitmap bitmap2Gray(Bitmap bmSrc) {        Bitmap faceIconGreyBitmap = Bitmap.createBitmap(bmSrc.getWidth(),                bmSrc.getHeight(), Bitmap.Config.ARGB_8888);        Canvas canvas = new Canvas(faceIconGreyBitmap);        Paint paint = new Paint();        ColorMatrix colorMatrix = new ColorMatrix();        colorMatrix.setSaturation(0);        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(                colorMatrix);        paint.setColorFilter(colorMatrixFilter);        canvas.drawBitmap(bmSrc, 0, 0, paint);        return faceIconGreyBitmap;    }}