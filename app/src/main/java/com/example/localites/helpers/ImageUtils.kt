package com.example.localites.helpers

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.media.ExifInterface
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import java.io.*
import java.text.DecimalFormat


object ImageUtils {
    private const val maxWidth = 612
    private const val maxHeight = 816

    @Throws(IOException::class)
    fun compressImage(
        imageFile: File,
        compressFormat: CompressFormat?,
        quality: Int
    ): File {
        var fileOutputStream: FileOutputStream? = null
        val file = File(
            Environment.getExternalStorageDirectory().toString() + "/Collude"
        ).parentFile
        if (!file.exists()) {
            file.mkdirs()
        }
        try {
            fileOutputStream = FileOutputStream(
                Environment.getExternalStorageDirectory().toString() + "/Collude"
            )
            // write the compressed bitmap at the destination specified by destinationPath.
            decodeSampledBitmapFromFile(imageFile.absolutePath)
                .compress(compressFormat, quality, fileOutputStream)
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush()
                fileOutputStream.close()
            }
        }
        return File(
            Environment.getExternalStorageDirectory().toString() + "/Collude"
        )
    }

    @Throws(IOException::class)
    fun decodeSampledBitmapFromFile(path: String?): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        // Calculate inSampleSize
        options.inSampleSize =
            calculateInSampleSizeNew(options, maxWidth, maxHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        var scaledBitmap = BitmapFactory.decodeFile(path, options)

        //check the rotation of the image and display it properly
        val exif: ExifInterface
        exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
        val matrix = Matrix()
        if (orientation == 6) {
            matrix.postRotate(90f)
        } else if (orientation == 3) {
            matrix.postRotate(180f)
        } else if (orientation == 8) {
            matrix.postRotate(270f)
        }
        scaledBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
        return scaledBitmap
    }

    private fun calculateInSampleSizeNew(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun saveFile(b: Bitmap, picName: String) {

        //FileOutputStream fos;
        try {
            val direct = File(
                Environment.getExternalStorageDirectory().toString() + "/Collude"
            )
            if (!direct.exists()) {
                val wallpaperDirectory = File("/sdcard/Collude/")
                wallpaperDirectory.mkdirs()
            }
            val fOut: OutputStream
            val filePath = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/Collude/" + picName
            )
            fOut = FileOutputStream(filePath)

            //fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
            b.compress(CompressFormat.JPEG, 80, fOut)
            fOut.close()
        } catch (e: FileNotFoundException) {
            Log.d("file", "file not found")
            e.printStackTrace()
        } catch (e: IOException) {
            Log.d("file", "io exception")
            e.printStackTrace()
        }
    }

    fun loadBitmap(picName: String): Bitmap? {
        var b: Bitmap? = null
        val fis: FileInputStream
        try {
            val filePath = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/Collude/" + picName
            )
            // b = scaleImage(filePath);
            fis = FileInputStream(filePath)
            //            fis = context.openFileInput(picName);
            b = BitmapFactory.decodeStream(fis)
            fis.close()
        } catch (e: FileNotFoundException) {
            Log.d("file", "file not found")
            e.printStackTrace()
        } catch (e: IOException) {
            Log.d("file", "io exception")
            e.printStackTrace()
        }

        return b
    }

    fun deleteFile(context: Context?, fileName: String): Boolean {
        val file = File(
            Environment.getExternalStorageDirectory().toString() + "/Collude/" + fileName
        )

//        File dir = context.getFilesDir();
//        File file = new File(dir, fileName);
        return file.delete()
    }

    fun filePath(context: Context?, fileName: String): String {
        val direct = File(
            Environment.getExternalStorageDirectory().toString() + "/Collude"
        )
        if (!direct.exists()) {
            val wallpaperDirectory = File("/sdcard/Collude/")
            wallpaperDirectory.mkdirs()
        }
        val file = File(
            Environment.getExternalStorageDirectory().toString() + "/Collude/" + fileName
        )

//        File dir = context.getFilesDir();
//        File file = new File(dir, fileName);
        return file.path
    }

    fun loadFile(context: Context?, picName: String): File {

        //        File dir = context.getFilesDir();
//        File file = new File(dir, picName);
        return File(
            Environment.getExternalStorageDirectory().toString() + "/Collude/" + picName
        )
    }

    fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups =
            (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#")
            .format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    // Compress image
    fun scaleImage(f: File): Bitmap? {
        val imagePath = f.path
        val maxHeight = 1280.0f
        val maxWidth = 1280.0f
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(imagePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
        val maxRatio = maxWidth / maxHeight
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth) as Int
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight) as Int
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        options.inJustDecodeBounds = false
        options.inDither = false
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
            bmp = BitmapFactory.decodeFile(imagePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        if (actualHeight > 0 && actualWidth > 0) {
            try {
                scaledBitmap =
                    Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
            } catch (exception: OutOfMemoryError) {
                exception.printStackTrace()
            }
            val ratioX = actualWidth / options.outWidth.toFloat()
            val ratioY = actualHeight / options.outHeight.toFloat()
            val middleX = actualWidth / 2.0f
            val middleY = actualHeight / 2.0f
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(scaledBitmap!!)
            canvas.setMatrix(scaleMatrix)
            canvas.drawBitmap(
                bmp!!,
                middleX - bmp.width / 2,
                middleY - bmp.height / 2,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
            bmp?.recycle()
        }

        /*ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }*/return scaledBitmap
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio =
                Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio =
                Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = width * height.toFloat()
        val totalReqPixelsCap = reqWidth * reqHeight * 2.toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    /* // scale image bitmap
    public static Bitmap scaleImage(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 512;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }*/
    // Roatate image
    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val retVal: Bitmap
        val matrix = Matrix()
        matrix.postRotate(angle)
        retVal =
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        return retVal
    }

    // Rounded image drawable
    fun roundedImage(context: Context, bitmap: Bitmap?): RoundedBitmapDrawable {
        val drawable =
            RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        drawable.isCircular = true
        return drawable
    }

    @Throws(IllegalArgumentException::class)
    fun convert(base64Str: String): Bitmap? {
        val decodedBytes: ByteArray = Base64.decode(
            base64Str.substring(base64Str.indexOf(",") + 1),
            Base64.DEFAULT
        )
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun convert(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun encodeToBase64(image: Bitmap): ByteArray {
        val byteArrayOS = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOS)
        return byteArrayOS.toByteArray()
    }

     fun decodeBase64(completeImageData: String): Bitmap? {

        val imageDataBytes =
            completeImageData.substring(completeImageData.indexOf(",") + 1)
        val stream: InputStream = ByteArrayInputStream(
            Base64.decode(
                imageDataBytes.toByteArray(), Base64.DEFAULT
            )
        )
        val bitmap = BitmapFactory.decodeStream(stream)
        return bitmap
    }
}