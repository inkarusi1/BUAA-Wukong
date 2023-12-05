package com.example.navbar.ui.home;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.navbar.util.CacheImageBean;
import com.example.navbar.util.MyBitmapUtils;
import com.example.navbar.util.PermissionUtils;
import com.example.navbar.util.Speaker;
import com.example.navbar.util.TourUnit;
import com.example.navbar.R;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoicePool;
import com.zlylib.fileselectorlib.FileSelector;
import com.zlylib.fileselectorlib.utils.Const;
import com.zlylib.mypermissionlib.RequestListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TourFragment extends Fragment {

    //显示fragment_dashboard,并监听button1到button4的点击事件
    private Button selectButton;
    private Button importButton; //导入按钮
    private Button playButton; //播放按钮
    private Button pauseButton; //暂停按钮
    private TextView title;
    private TextView brief;
    private ImageView image;
    private Speaker speaker;

    private ArrayList<TourUnit> tourUnits = new ArrayList<TourUnit>();
    private HashMap<String, ArrayList<TourUnit>> tourMap = new HashMap<String, ArrayList<TourUnit>>();
    private HashMap<String, ArrayList<String>> tourTitlesMap = new HashMap<String, ArrayList<String>>();
    private List<String> locaTitles = new ArrayList<String>();
    private List<List<String>> scenicList = new ArrayList<>();

    private String tv_backResult = "";
    private MyBitmapUtils myBitmapUtils;
    private OptionsPickerView mOptionsPicker;

    private Integer nowTour = -1;
    private Integer nowLoca = -1;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tour, container, false);
        selectButton = view.findViewById(R.id.button1);
        importButton = view.findViewById(R.id.button2);
        playButton = view.findViewById(R.id.button3);
        pauseButton = view.findViewById(R.id.button4);
        title = view.findViewById(R.id.title);
        brief = view.findViewById(R.id.brief);
        image = view.findViewById(R.id.image);
        speaker = new Speaker();
        Context context = getContext();

        initPickerView();//初始化选择器
        if (nowTour + nowLoca != -2) {
            title.setText((String) scenicList.get(nowLoca).get(nowTour) );
            brief.setText(Objects.requireNonNull(tourMap.get(locaTitles.get(nowLoca))).get(nowTour).getBrief());
            setImage(Objects.requireNonNull(tourMap.get(locaTitles.get(nowLoca))).get(nowTour).getImageId(), Objects.requireNonNull(tourMap.get(locaTitles.get(nowLoca))).get(nowTour).getImgUrl());
        }

        // 根据Android版本判断是否申请文件访问权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);

            }
        }

        //测试：向tourUnits中添加数据，使用了repeat请确保jdk版本在11以上，否则记得删除该行
        if (tourUnits.isEmpty()) {
            tourUnits.add(new TourUnit("第一个景点", "这是第一个景点简介\n".repeat(8), "这是第一个景点简介\n".repeat(20)));
            tourUnits.add(new TourUnit("第二个景点", "这是第二个景点简介\n".repeat(8), "这是第二个景点简介\n".repeat(20)));
            tourUnits.add(new TourUnit("第三个景点", "这是第三个景点简介\n".repeat(8), "这是第三个景点简介\n".repeat(20), "https://s1.imagehub.cc/images/2023/11/23/92ecfd2d768fe4d9e9dc401ce67b74f2.png"));
            tourMap.put("位置1", tourUnits);
            tourUnits = new ArrayList<TourUnit>();
            tourUnits.add(new TourUnit("第四个景点", "这是第四个景点简介\n".repeat(8), "这是第一个景点简介\n".repeat(20)));
            tourUnits.add(new TourUnit("第五个景点", "这是第五个景点简介\n".repeat(8), "这是第二个景点简介\n".repeat(20)));
            tourUnits.add(new TourUnit("第六个景点", "这是第六个景点简介\n".repeat(8), "这是第三个景点简介\n".repeat(20), "https://s1.imagehub.cc/images/2023/11/23/92ecfd2d768fe4d9e9dc401ce67b74f2.png"));
            tourMap.put("位置2", tourUnits);
            //更新标题
            setTitles();
        }
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用
                //条件选择器
                OptionsPickerView<Object> pvOptions = new OptionsPickerBuilder(context, new OnOptionsSelectListener() {
                    @Override
                    public void onOptionsSelect(int options1, int option2, int options3 ,View v) { // option1代表地点，option2代表景点\
                        //设置标题
                        //title.setText((String) locaTitles.get(options1));
                        title.setText((String) scenicList.get(options1).get(option2) );
                        //设置简介
                        //brief.setText(tourUnits.get(options1).getBrief());
                        brief.setText(tourMap.get(locaTitles.get(options1)).get(option2).getBrief());
                        //设置当前景点
                        nowLoca = options1;
                        nowTour = option2;
                        //图片
                        //setImage(tourUnits.get(options1).getImageId(), tourUnits.get(options1).getImgUrl());
                        setImage(tourMap.get(locaTitles.get(options1)).get(option2).getImageId(), tourMap.get(locaTitles.get(options1)).get(option2).getImgUrl());
                    }
                }).build();
                pvOptions.setPicker((List) locaTitles);
                pvOptions.setPicker((List) locaTitles,  (List)scenicList);
                pvOptions.show();
            }
        });

        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //导入
                importTours(v);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放
                play(v);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //暂停
                pause(v);
            }
        });


        return view;
    }

    //视图将要销毁时
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //缓存title、brief、image
        //缓存title
        //缓存brief
        //缓存image

    }

    private void setImage(int imageId, String imgUrl) {
        //先判断Url是否为null
        if (imgUrl == null) {
            image.setImageResource(imageId);
        } else {
            //使用Glide加载图片
            //Glide.with(this).load(imgUrl).into(image);
            myBitmapUtils = new MyBitmapUtils(getContext());
            Bitmap imageFile = myBitmapUtils.disPlay(image, imgUrl);
            CacheImageBean imageGrid = new CacheImageBean();
            imageGrid.setBitmapFile(imageFile);
            imageGrid.setGridImage(image);
            Message msg = Message.obtain();
            msg.obj = imageGrid;
            msg.what = 100;
            handler.sendMessage(msg);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    CacheImageBean obj = (CacheImageBean) msg.obj;
                    obj.getGridImage().setImageBitmap(obj.getBitmapFile());
                    //Glide加载图片,直接加载本地图片所在文件
//                    Glide.with(mContext).load((Bitmap)obj.getBitmapFile()).into(obj.getGridImage());
                    //Glide加载图片,用网络图片的url加载图片
//                    ivImage.setImageBitmap((Bitmap)msg.obj);
            }
        }
    };
    private void setTitles() {
        locaTitles.clear();
        locaTitles.addAll(tourMap.keySet());
        scenicList.clear();
        for (String loca :
                locaTitles) {
            ArrayList<String> scenicTitles = new ArrayList<String>();
            for (TourUnit tourUnit :
                    Objects.requireNonNull(tourMap.get(loca))) {
                scenicTitles.add(tourUnit.getTitle());
            }
            scenicList.add(scenicTitles);
        }
    }

    //播放
    public void play(View view) {
        //播放解说稿
        Toast.makeText(getContext(), "播放解说", Toast.LENGTH_SHORT).show();
        if (nowTour != -1) {
            String plain = Objects.requireNonNull(tourMap.get(locaTitles.get(nowLoca))).get(nowTour).getExplain();
            speaker.speak(plain);
        } else {
            VoicePool.get().playTTs("请选择场景哦~", Priority.NORMAL, null);
        }
    }

    //暂停
    public void pause(View view) {
        //暂停解说稿
        Toast.makeText(getContext(), "暂停解说", Toast.LENGTH_SHORT).show();
        speaker.stop();
    }

    //导入
    public void importTours(View view) {
        PermissionUtils.request(new RequestListener() {
            @Override
            public void onSuccess() {
                //导入解说稿
                openOustomizeTitle(1);


            }

            @Override
            public void onFailed() {
                Toast.makeText(getContext(), "权限申请失败", Toast.LENGTH_SHORT).show();
            }
        }, getContext(), PermissionUtils.PermissionGroup.PERMISSIONS_STORAGE);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION) != null) {
                    ArrayList<String> essFileList = data.getStringArrayListExtra(Const.EXTRA_RESULT_SELECTION);
                    StringBuilder builder = new StringBuilder();
                    for (String file :
                            essFileList) {
                        builder.append(file); //.append("\n")
                        break;
                    }
                    tv_backResult = builder.toString();
                    fileProcess();
                    //title.setText(tv_backResult);
                }
            }
        }
    }

    public void openOustomizeTitle(int retCode) {
        FileSelector.from(this)
                .setMaxCount(10) //设置最大选择数
                .setFileTypes("json","txt") //设置文件类型
                .setSortType(FileSelector.BY_NAME_ASC) //设置名字排序
                .requestCode(1) //设置返回码
                .start();
    }

    public void fileProcess() { //处理导入解说功能
        //文件处理
        Toast.makeText(getContext(), "文件处理中", Toast.LENGTH_SHORT).show();
        //将tv_backResult路径下的文件存储到str中
        String str = "";
        File path = new File(tv_backResult);
        //取出tv_backResult中的文件名: 通过 / 分割tv_backResult，取出最后一项
        String fileName = tv_backResult.split("/")[tv_backResult.split("/").length - 1];
        //去除后缀名
        fileName = fileName.substring(0,fileName.lastIndexOf("."));
        //将path路径下的文件存储到str中
        str = getFileContent(path);
        //brief.setText(str);
        //使用json格式解析str
        try {
            JSONObject jo = new JSONObject(str);
            //取出json结构中的locaName
            String locaName = fileName;
            if (jo.has("locaName"))
                locaName = jo.getString("locaName");
            int i = 1;
            ArrayList<TourUnit> tmp = new ArrayList<TourUnit>();
            while (jo.has(String.valueOf(i))) {
                String title = jo.getJSONObject(String.valueOf(i)).getString("title");
                String brief = jo.getJSONObject(String.valueOf(i)).getString("brief");
                String explain = jo.getJSONObject(String.valueOf(i)).getString("explain");
                String imgUrl = jo.getJSONObject(String.valueOf(i)).getString("imageUrl");
                tmp.add(new TourUnit(title, brief, explain, imgUrl));
                i++;
            }
            tourMap.put(locaName, tmp);
            //更新标题
            setTitles();
            Toast.makeText(getContext(), "导入成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //读取指定目录下的所有TXT文件的文件内容
    protected String getFileContent(File path) {
        String content  = "";
        try {
            InputStream instream = new FileInputStream(path);
            InputStreamReader inputreader = new InputStreamReader(instream, StandardCharsets.UTF_8);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line="";
            //分行读取
            while (( line = buffreader.readLine()) != null) {
                content += line + "\n";
            }
            instream.close();
        }
        catch (java.io.FileNotFoundException e) {
            Log.d("TestFile-eee", "The File doesn't not exist.");
        }
        catch (IOException e)  {
            Log.d("TestFile-eee", Objects.requireNonNull(e.getMessage()));
        }
        return content ;
    }

    //读取指定目录下的所有TXT文件的文件名
    private String getFileName(File[] files) {
        String str = "";
        if (files != null) {	// 先判断目录是否为空，否则会报空指针
            for (File file : files) {
                if (file.isDirectory()){//检查此路径名的文件是否是一个目录(文件夹)
                    Log.i("zeng-eee", "若是文件目录。继续读1" +file.getName().toString()+file.getPath().toString());
                    getFileName(file.listFiles());
                    Log.i("zeng-eee", "若是文件目录。继续读2" +file.getName().toString()+ file.getPath().toString());
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(".txt")) {
                        String s=fileName.substring(0,fileName.lastIndexOf(".")).toString();
                        Log.i("zeng-eee", "文件名txt：：   " + s);
                        str += fileName.substring(0,fileName.lastIndexOf("."))+"\n";
                    }
                }
            }
        }
        return str;
    }


    //initPickerView
    private void initPickerView() {

    }

}