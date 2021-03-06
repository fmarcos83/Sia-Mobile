package vandyke.siamobile.util

import android.content.Context
import android.os.Environment
import android.widget.Toast
import vandyke.siamobile.SiaMobileApplication
import vandyke.siamobile.prefs
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object StorageUtil {
    val isSiadSupported: Boolean
        get() = SiaMobileApplication.abi == "arm64"

    // will return null if the abi is an unsupported one and therefore there is not a binary for it
    fun copyBinary(filename: String, context: Context, bit32: Boolean): File? {
        try {
            val `in`: InputStream
            val result: File
            if (bit32) {
                `in` = context.assets.open(filename + "-" + SiaMobileApplication.abi32)
                result = File(context.filesDir, filename + "-" + SiaMobileApplication.abi32)
            } else {
                `in` = context.assets.open(filename + "-" + SiaMobileApplication.abi)
                result = File(context.filesDir, filename + "-" + SiaMobileApplication.abi)
            }
            if (result.exists())
                return result
            val out = FileOutputStream(result)
            val buf = ByteArray(1024)
            var length: Int = `in`.read(buf)
            while (length > 0) {
                out.write(buf, 0, length)
                length = `in`.read(buf)
            }
            result.setExecutable(true)
            `in`.close()
            out.close()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun getWorkingDirectory(context: Context?): File? {
        if (context == null)
            return null
        var result: File?
        if (prefs.useExternal) {
            result = context.getExternalFilesDir(null)
            if (result == null) { // external storage not found
                Toast.makeText(context, "No external storage found. Using internal", Toast.LENGTH_LONG).show()
                result = context.filesDir
            }
        } else
            result = context.filesDir
        return result
    }

    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    fun externalStorageStateDescription(): String {
        when (Environment.getExternalStorageState()) {
            Environment.MEDIA_BAD_REMOVAL -> return "external storage was previously removed before being unmounted"
            Environment.MEDIA_CHECKING -> return "external storage is present but being disk-checked"
            Environment.MEDIA_EJECTING -> return "external storage is in the process of ejecting"
            Environment.MEDIA_MOUNTED -> return "external storage is present and mounted with read/write access"
            Environment.MEDIA_MOUNTED_READ_ONLY -> return "external storage is present but mounted as read-only"
            Environment.MEDIA_NOFS -> return "external storage is present but is blank or using an unsupported filesystem"
            Environment.MEDIA_REMOVED -> return "external storage is not present"
            Environment.MEDIA_SHARED -> return "external storage is present but being shared via USB"
            Environment.MEDIA_UNKNOWN -> return "external storage is in an unknown state"
            Environment.MEDIA_UNMOUNTABLE -> return "external storage is present but cannot be mounted. May be corrupted"
            Environment.MEDIA_UNMOUNTED -> return "external storage is present but unmounted"
            else -> return "external storage state missed all cases"
        }
    }
}