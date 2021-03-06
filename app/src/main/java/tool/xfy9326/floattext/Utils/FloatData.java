package tool.xfy9326.floattext.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import tool.xfy9326.floattext.Method.IOMethod;
import tool.xfy9326.floattext.Tool.FormatArrayList;

/*
 数据操作
 带有VersionFix的方法均为数据版本升级的适配
 */

public class FloatData {
    private final SharedPreferences spdatat;
    private final SharedPreferences.Editor speditt;
    private final SharedPreferences spdata;
    private final SharedPreferences.Editor spedit;
    private final App utils;
    private int DataNum = 0;

    public FloatData(Context ctx) {
        utils = ((App) ctx.getApplicationContext());
        spdatat = ctx.getSharedPreferences("FloatTextList", Activity.MODE_PRIVATE);
        speditt = spdatat.edit();
        speditt.apply();
        spdata = ctx.getSharedPreferences("FloatShowList", Activity.MODE_PRIVATE);
        spedit = spdata.edit();
        spedit.apply();
    }

    //Base64转换
    private static ArrayList<String> TextArr_decode(ArrayList<String> str) {
        ArrayList<String> output = new ArrayList<>();
        output.addAll(str);
        if (str.size() > 0) {
            for (int i = 0; i < str.size(); i++) {
                String result;
                try {
                    result = new String(Base64.decode(str.get(i).getBytes(), Base64.NO_WRAP));
                } catch (IllegalArgumentException e1) {
                    try {
                        result = new String(Base64.decode(str.get(i), Base64.DEFAULT));
                    } catch (IllegalArgumentException e2) {
                        result = str.get(i);
                    }
                }
                output.set(i, result);
            }
        }
        return output;
    }

    private static ArrayList<String> TextArr_encode(ArrayList<String> str) {
        ArrayList<String> output = new ArrayList<>();
        output.addAll(str);
        if (str.size() > 0) {
            for (int i = 0; i < str.size(); i++) {
                String result = Base64.encodeToString(str.get(i).getBytes(), Base64.NO_WRAP);
                output.set(i, result);
            }
        }
        return output;
    }

    //保存
    public void savedata() {
        FloatTextUtils textutils = utils.getTextutil();
        spedit.putInt("Version", StaticNum.FloatDataVersion);
        speditt.putString("TextArray", TextArr_encode(textutils.getTextShow()).toString());
        spedit.putString("ColorArray", textutils.getColorShow().toString());
        spedit.putString("ThickArray", textutils.getThickShow().toString());
        spedit.putString("SizeArray", textutils.getSizeShow().toString());
        spedit.putString("ShowArray", textutils.getShowFloat().toString());
        spedit.putString("LockArray", textutils.getLockPosition().toString());
        spedit.putString("PositionArray", textutils.getPosition().toString());
        spedit.putString("TopArray", textutils.getTextTop().toString());
        spedit.putString("AutoTopArray", textutils.getAutoTop().toString());
        spedit.putString("MoveArray", textutils.getTextMove().toString());
        spedit.putString("SpeedArray", textutils.getTextSpeed().toString());
        spedit.putString("ShadowArray", textutils.getTextShadow().toString());
        spedit.putString("ShadowXArray", textutils.getTextShadowX().toString());
        spedit.putString("ShadowYArray", textutils.getTextShadowY().toString());
        spedit.putString("ShadowRadiusArray", textutils.getTextShadowRadius().toString());
        spedit.putString("BackgroundColorArray", textutils.getBackgroundColor().toString());
        spedit.putString("TextShadowColorArray", textutils.getTextShadowColor().toString());
        spedit.putString("FloatSizeArray", textutils.getFloatSize().toString());
        spedit.putString("FloatLongArray", textutils.getFloatLong().toString());
        spedit.putString("FloatWideArray", textutils.getFloatWide().toString());
        spedit.putString("NotifyControlArray", textutils.getNotifyControl().toString());
        spedit.apply();
        speditt.apply();
    }

    //获取
    public void getSaveArrayData() {
        int version = spdata.getInt("Version", 0);
        ArrayList<String> textarr = new ArrayList<>();
        VersionFix_1(version, textarr);
        VersionFix_2(version, textarr);
        VersionFix_4(version, textarr);
        DataNum = textarr.size();
        FloatTextUtils textutils = utils.getTextutil();
        textutils.setTextShow(textarr);
        textutils.setSizeShow(NewFloatKey(spdata.getString("SizeArray", "[]"), "20.0"));
        textutils.setColorShow(NewIntegerKey(spdata.getString("ColorArray", "[]"), "-61441"));
        textutils.setThickShow(NewBooleanKey(spdata.getString("ThickArray", "[]"), "false"));
        textutils.setShowFloat(NewBooleanKey(spdata.getString("ShowArray", "[]"), "true"));
        textutils.setLockPosition(NewBooleanKey(spdata.getString("LockArray", "[]"), "false"));
        textutils.setPosition(NewStringKey(spdata.getString("PositionArray", "[]"), "50_50"));
        textutils.setTextTop(NewBooleanKey(spdata.getString("TopArray", "[]"), "false"));
        textutils.setAutoTop(NewBooleanKey(spdata.getString("AutoTopArray", "[]"), "true"));
        textutils.setTextMove(NewBooleanKey(spdata.getString("MoveArray", "[]"), "false"));
        textutils.setTextSpeed(NewIntegerKey(spdata.getString("SpeedArray", "[]"), "5"));
        textutils.setTextShadow(NewBooleanKey(spdata.getString("ShadowArray", "[]"), "false"));
        textutils.setTextShadowX(NewFloatKey(spdata.getString("ShadowXArray", "[]"), "10.0"));
        textutils.setTextShadowY(NewFloatKey(spdata.getString("ShadowYArray", "[]"), "10.0"));
        textutils.setTextShadowRadius(NewFloatKey(spdata.getString("ShadowRadiusArray", "[]"), "5.0"));
        textutils.setBackgroundColor(NewIntegerKey(spdata.getString("BackgroundColorArray", "[]"), "16777215"));
        textutils.setTextShadowColor(NewIntegerKey(spdata.getString("TextShadowColorArray", "[]"), "1660944384"));
        textutils.setFloatSize(NewBooleanKey(spdata.getString("FloatSizeArray", "[]"), "false"));
        textutils.setFloatLong(NewFloatKey(spdata.getString("FloatLongArray", "[]"), "100"));
        textutils.setFloatWide(NewFloatKey(spdata.getString("FloatWideArray", "[]"), "100"));
        textutils.setNotifyControl(NewBooleanKey(spdata.getString("NotifyControlArray", "[]"), "true"));
        VersionFix_3(version, textutils);
        utils.setTextutil(textutils);
    }

    private void VersionFix_1(int version, ArrayList<String> textarr) {
        String text = spdatat.getString("TextArray", "[]");
        if (version < 1) {
            textarr.addAll(FormatArrayList.StringToStringArrayList(text));
            speditt.putString("TextArray", TextArr_encode(textarr).toString());
            speditt.commit();
            updateVersion(1);
        } else {
            textarr.addAll(TextArr_decode(FormatArrayList.StringToStringArrayList(text)));
        }
    }

    private void VersionFix_2(int version, ArrayList<String> textarr) {
        if (version < 2) {
            String text_v = spdata.getString("TextArray", "[]");
            textarr.clear();
            if (version < 1) {
                textarr.addAll(FormatArrayList.StringToStringArrayList(text_v));
            }
            textarr.addAll(TextArr_decode(FormatArrayList.StringToStringArrayList(text_v)));
            spedit.remove("TextArray");
            speditt.putString("TextArray", TextArr_encode(textarr).toString());
            spedit.commit();
            speditt.commit();
            updateVersion(2);
        }
    }

    private void VersionFix_3(int version, FloatTextUtils textutils) {
        if (version < 3) {
            ArrayList<Float> longtemp = new ArrayList<>();
            longtemp.addAll(textutils.getFloatLong());
            ArrayList<Float> widetemp = new ArrayList<>();
            widetemp.addAll(textutils.getFloatWide());
            textutils.getFloatLong().clear();
            textutils.getFloatLong().addAll(widetemp);
            textutils.getFloatWide().clear();
            textutils.getFloatWide().addAll(longtemp);
            updateVersion(3);
        }
    }

    private void VersionFix_4(int version, ArrayList<String> textarr) {
        if (version < 4) {
            for (int i = 0; i < textarr.size(); i++) {
                if (textarr.get(i).contains("Origination")) {
                    textarr.set(i, textarr.get(i).replace("Origination", "Orientation"));
                }
            }
            speditt.putString("TextArray", TextArr_encode(textarr).toString());
            speditt.commit();
            updateVersion(4);
        }
    }

    //输出
    public boolean OutputData(String path, int VersionCode) {
        String jsonresult;
        JSONObject mainobject = new JSONObject();
        JSONObject dataobject = new JSONObject();
        JSONObject textobject = new JSONObject();
        try {
            textobject.put("TextArray", spdatat.getString("TextArray", "[]"));

            SetTextData(dataobject);

            mainobject.put("FloatText_Version", VersionCode);
            mainobject.put("Data_Version", StaticNum.FloatDataVersion);
            mainobject.put("Text", textobject);
            mainobject.put("Data", dataobject);

            jsonresult = mainobject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return IOMethod.writefile(path, jsonresult);
    }

    private void SetTextData(JSONObject dataobject) throws JSONException {
        xmltojson(dataobject, "SizeArray");
        xmltojson(dataobject, "ColorArray");
        xmltojson(dataobject, "ThickArray");
        xmltojson(dataobject, "ShowArray");
        xmltojson(dataobject, "LockArray");
        xmltojson(dataobject, "PositionArray");
        xmltojson(dataobject, "TopArray");
        xmltojson(dataobject, "AutoTopArray");
        xmltojson(dataobject, "MoveArray");
        xmltojson(dataobject, "SpeedArray");
        xmltojson(dataobject, "ShadowArray");
        xmltojson(dataobject, "ShadowXArray");
        xmltojson(dataobject, "ShadowYArray");
        xmltojson(dataobject, "ShadowRadiusArray");
        xmltojson(dataobject, "BackgroundColorArray");
        xmltojson(dataobject, "TextShadowColorArray");
        xmltojson(dataobject, "FloatSizeArray");
        xmltojson(dataobject, "FloatLongArray");
        xmltojson(dataobject, "FloatWideArray");
        xmltojson(dataobject, "NotifyControlArray");
    }

    //导入
    public boolean InputData(String path) {
        File bak = new File(path);
        if (!bak.exists() && bak.isDirectory()) {
            return false;
        } else {
            String[] data = IOMethod.readfile(bak);
            String str = "";
            for (String aData : data) {
                str += aData;
            }
            return !str.equalsIgnoreCase("Failed") && InputDataAction(str);
        }
    }

    private boolean InputDataAction(String str) {
        if (!Objects.equals(str, "") && !str.startsWith("error")) {
            try {
                JSONObject mainobject = new JSONObject(str);
                //int FloatText_Version = mainobject.getInt("FloatText_Version");
                int Data_Version = mainobject.getInt("Data_Version");
                JSONObject dataobject = mainobject.getJSONObject("Data");
                JSONObject textobject = mainobject.getJSONObject("Text");

                String text = textobject.getString("TextArray");
                String oldtext = spdatat.getString("TextArray", "[]");
                if (oldtext.equalsIgnoreCase("[]")) {
                    oldtext = text;
                } else {
                    oldtext = CombineArrayString(oldtext, text);
                }
                speditt.putString("TextArray", oldtext);

                savetofile(dataobject);

                JsonVersionFix_1(Data_Version);

                spedit.commit();
                speditt.commit();
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    //导入数据保存
    private void savetofile(JSONObject dataobject) throws JSONException {
        Iterator it = dataobject.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            if (spdata.contains(key)) {
                String old = spdata.getString(key, "[]");
                String get = dataobject.getString(key);
                if (old.equalsIgnoreCase("[]")) {
                    old = get;
                } else {
                    old = CombineArrayString(old, get);
                }
                spedit.putString(key, old);
            }
        }
    }

    //导入数据合并
    private String CombineArrayString(String a1, String a2) {
        if (a1.length() == 2) {
            return a2;
        }
        return a1.substring(0, a1.length() - 1) + ", " + a2.substring(1, a2.length());
    }

    private void JsonVersionFix_1(int version) {
        if (version < 3) {
            updateVersion(2);
        }
    }

    private void xmltojson(JSONObject obj, String name) throws JSONException {
        obj.put(name, spdata.getString(name, "[]"));
    }

    private void updateVersion(int i) {
        spedit.putInt("Version", i);
        spedit.commit();
    }

    //新的数据值适配
    private ArrayList<String> NewStringKey(String fix, String def) {
        fix = NewKey(fix, def);
        ArrayList<String> res = FormatArrayList.StringToStringArrayList(fix);
        return FixKey(res, def);
    }

    private ArrayList<Integer> NewIntegerKey(String fix, String def) {
        fix = NewKey(fix, def);
        ArrayList<Integer> res = FormatArrayList.StringToIntegerArrayList(fix);
        return FixKey(res, Integer.valueOf(def));
    }

    private ArrayList<Float> NewFloatKey(String fix, String def) {
        fix = NewKey(fix, def);
        ArrayList<Float> res = FormatArrayList.StringToFloatArrayList(fix);
        return FixKey(res, Float.valueOf(def));
    }

    private ArrayList<Boolean> NewBooleanKey(String fix, String def) {
        fix = NewKey(fix, def);
        ArrayList<Boolean> res = FormatArrayList.StringToBooleanArrayList(fix);
        return FixKey(res, Boolean.valueOf(def));
    }

    private String NewKey(String fix, String def) {
        if (fix.equalsIgnoreCase("[]") && DataNum != 0) {
            ArrayList<String> str = new ArrayList<>();
            for (int i = 0; i < DataNum; i++) {
                str.add(def);
            }
            fix = str.toString();
        }
        return fix;
    }

    private ArrayList<String> FixKey(ArrayList<String> str, String def) {
        while (str.size() < DataNum) {
            str.add(def);
        }
        return str;
    }

    private ArrayList<Float> FixKey(ArrayList<Float> str, Float def) {
        while (str.size() < DataNum) {
            str.add(def);
        }
        return str;
    }

    private ArrayList<Integer> FixKey(ArrayList<Integer> str, Integer def) {
        while (str.size() < DataNum) {
            str.add(def);
        }
        return str;
    }

    private ArrayList<Boolean> FixKey(ArrayList<Boolean> str, Boolean def) {
        while (str.size() < DataNum) {
            str.add(def);
        }
        return str;
    }

}
