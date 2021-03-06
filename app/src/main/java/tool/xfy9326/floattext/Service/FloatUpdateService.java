package tool.xfy9326.floattext.Service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

import tool.xfy9326.floattext.R;
import tool.xfy9326.floattext.Utils.StaticNum;

public class FloatUpdateService extends Service {
    private long DownloadTaskId = -1;
    private String downloadPath = null;
    private DownloadManager downloadManager;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();
        }
    };

    @Override
    public void onCreate() {
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent p1) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getIntExtra("TYPE", 0) == StaticNum.FLOATUPDATE_START_DOWNLOAD) {
                String url = intent.getStringExtra("URL");
                DownloadFile(url);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void DownloadFile(String url) {
        Uri uri = Uri.parse(url);
        String downloadFileName = url.substring(url.lastIndexOf("/"));
        downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FloatText/Download/" + downloadFileName;
        File existfile = new File(downloadPath);
        if (existfile.exists()) {
            installAPK(existfile, true);
        } else {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
            request.setMimeType(mimeString);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setVisibleInDownloadsUi(true);
            request.setDestinationInExternalPublicDir("/FloatText/Download/", downloadFileName);
            DownloadTaskId = downloadManager.enqueue(request);
            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(DownloadTaskId);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    Toast.makeText(this, R.string.updater_success, Toast.LENGTH_SHORT).show();
                    installAPK(new File(downloadPath), false);
                    break;
                case DownloadManager.STATUS_FAILED:
                    Toast.makeText(this, R.string.updater_failed, Toast.LENGTH_SHORT).show();
                    unregisterReceiver(receiver);
                    stopSelf();
                    break;
            }
        }
    }

    private void installAPK(File file, boolean existInstall) {
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(this, "tool.xfy9326.floattext.fileprovider", file);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
            if (!existInstall) {
                unregisterReceiver(receiver);
            }
            stopSelf();
        }
    }
}
