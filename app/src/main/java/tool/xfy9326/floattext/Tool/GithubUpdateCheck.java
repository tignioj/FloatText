package tool.xfy9326.floattext.Tool;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import tool.xfy9326.floattext.Method.ActivityMethod;
import tool.xfy9326.floattext.R;
import tool.xfy9326.floattext.Service.FloatUpdateService;
import tool.xfy9326.floattext.Utils.StaticNum;

//Github更新检测

public class GithubUpdateCheck {
    private final Context ctx;
    private String UserName = null;
    private String ProjectName = null;
    private boolean DataPrepared = false;
    private String Release_URL = null;
    private Thread getData = null;
    private String Release_Market_URL = null;
    private boolean showLoadingDialog = true;
    private boolean showMarketDownload = false;

    private AlertDialog loading = null;

    private String JSON_UpdateData = null;
    private int JSON_VersionCode = -1;
    private String JSON_VersionName = null;
    private String JSON_DownloadURL = null;
    private String JSON_UpdateTime = null;
    private String JSON_UpdateSize = null;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                JSONObject release_Data = new JSONObject(msg.obj.toString());
                AnalysisJSON(release_Data);

                if (loading != null) {
                    loading.dismiss();
                    loading = null;
                }
                if (ActivityMethod.getVersionCode(ctx) < JSON_VersionCode) {
                    showMessageDialog();
                } else {
                    if (showLoadingDialog) {
                        Toast.makeText(ctx, R.string.updater_noupdate, Toast.LENGTH_SHORT).show();
                    }
                }
                DataPrepared = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public GithubUpdateCheck(Context ctx) {
        this.ctx = ctx;
    }

    private static String readStream(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        is.close();
        return new String(baos.toByteArray(), "utf-8");
    }

    private static String getCurrentVersion(Context ctx) {
        return "v" + ActivityMethod.getVersionName(ctx) + " (" + ActivityMethod.getVersionCode(ctx) + ")";
    }

    //市场下载设置
    public void setMarketDownload(boolean showMarketDownload, String url) {
        this.showMarketDownload = showMarketDownload;
        this.Release_Market_URL = url;
    }

    //Github数据设置
    public void setProjectData(String UserName, String ProjectName) {
        this.UserName = UserName;
        this.ProjectName = ProjectName;
        DataPrepared = false;
    }

    private void PrepareData() {
        if (ProjectName != null && UserName != null) {
            Release_URL = "https://api.github.com/repos/" + UserName + "/" + ProjectName + "/releases/latest";
        } else {
            DataPrepared = false;
        }
    }

    //显示提示窗口 开始检测
    public void showUpdateInfoDialog(boolean showLoadingDialog) {
        PrepareData();
        this.showLoadingDialog = showLoadingDialog;
        if (DataPrepared) {
            if (ActivityMethod.getVersionCode(ctx) < JSON_VersionCode) {
                showMessageDialog();
            } else {
                if (showLoadingDialog) {
                    Toast.makeText(ctx, R.string.updater_noupdate, Toast.LENGTH_SHORT).show();
                }
            }
            DataPrepared = false;
        } else {
            getJSON(Release_URL);
            if (showLoadingDialog) {
                showLoadingDialog();
            }
        }
    }

    private void showLoadingDialog() {
        AlertDialog.Builder load = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View layout = inflater.inflate(R.layout.dialog_loading, null);
        load.setCancelable(false);
        load.setTitle(R.string.updater_loadupdate);
        load.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i) {
                if (getData != null) {
                    getData.interrupt();
                }
            }
        });
        load.setView(layout);
        loading = load.show();
    }

    private void showMessageDialog() {
        AlertDialog.Builder msg = new AlertDialog.Builder(ctx);
        msg.setTitle(R.string.updater_findupdate);
        msg.setMessage(MsgBuild());
        msg.setPositiveButton(R.string.updater_getupdate, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i) {
                if (showMarketDownload) {
                    DownloadChoose();
                } else {
                    GithubDownload();
                }
            }
        });
        msg.setNegativeButton(R.string.cancel, null);
        msg.show();
    }

    private void DownloadChoose() {
        AlertDialog.Builder downloadchoose = new AlertDialog.Builder(ctx);
        downloadchoose.setItems(ctx.getResources().getStringArray(R.array.update_list), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i) {
                if (i == 0) {
                    MarketDownload();
                } else if (i == 1) {
                    GithubDownload();
                }
            }
        });
        downloadchoose.show();
    }

    private void GithubDownload() {
        Intent intent = new Intent();
        intent.setClass(ctx, FloatUpdateService.class);
        intent.putExtra("URL", JSON_DownloadURL);
        intent.putExtra("TYPE", StaticNum.FLOATUPDATE_START_DOWNLOAD);
        Toast.makeText(ctx, R.string.updater_download, Toast.LENGTH_SHORT).show();
        ctx.startService(intent);
    }

    private void MarketDownload() {
        if (Release_Market_URL != null) {
            Uri url = Uri.parse(Release_Market_URL.trim());
            Intent intent = new Intent(Intent.ACTION_VIEW, url);
            ctx.startActivity(intent);
        }
    }

    private String MsgBuild() {
        return ctx.getString(R.string.updater_version) + getCurrentVersion(ctx) + " > " + JSON_VersionName + " (" + JSON_VersionCode + ")" + "\n" + ctx.getString(R.string.updater_time) + JSON_UpdateTime + "\n" + ctx.getString(R.string.updater_size) + ActivityMethod.formatSize(ctx, JSON_UpdateSize) + "\n" + ctx.getString(R.string.updater_data) + "\n" + JSON_UpdateData;
    }

    private void AnalysisJSON(JSONObject obj) {
        try {
            JSON_UpdateData = obj.getString("body");
            JSON_VersionCode = Integer.parseInt(obj.getString("tag_name"));
            JSON_VersionName = obj.getString("name");
            JSON_UpdateTime = obj.getString("published_at").replace("T", " ").replace("Z", " ");

            JSONObject asset = obj.getJSONArray("assets").getJSONObject(0);
            JSON_UpdateSize = (asset.getInt("size") + "");
            JSON_DownloadURL = asset.getString("browser_download_url");

            DataPrepared = true;
        } catch (JSONException e) {
            e.printStackTrace();
            DataPrepared = false;
        }
    }

    private void getJSON(final String PATH) {
        getData = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(PATH).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream is = conn.getInputStream();
                        String content = readStream(is);
                        Message msg = new Message();
                        msg.obj = content;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getData.start();
    }

}
