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
import com.example.navbar.MainActivity;
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
            initTourUnits();
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
                        title.setText((String) scenicList.get(options1).get(option2) );
                        //设置简介
                        brief.setText(tourMap.get(locaTitles.get(options1)).get(option2).getBrief());
                        //设置当前景点
                        nowLoca = options1; nowTour = option2;
                        //图片
                        setImage(tourMap.get(locaTitles.get(options1)).get(option2).getImageId(), tourMap.get(locaTitles.get(options1)).get(option2).getImgUrl());
                        // 在activity中设置对应的字段，方便CharFragment读取
                        MainActivity activity = (MainActivity) TourFragment.this.getActivity();
                        if (activity != null) {
                            activity.setScene(locaTitles.get(options1) + title.getText().toString(), brief.getText().toString());
                        }
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

    private void initTourUnits() {
        tourUnits.add(new TourUnit("总馆",
                "北京航空航天博物馆是中国第一座向公众开放的综合性航空科技博物馆。展区面积约8300平米，分为长空逐梦、银鹰巡空、神舟问天、空天走廊4个展区，馆藏300多件国内外公认的珍贵实物，通过高科技手段展示了航空航天原理以及人类飞天的历程。\n",
                "北京航空航天博物馆是中国第一座向公众开放的综合性航空科技博物馆，前身是成立于1985年的北京航空馆，是在北航飞机结构陈列室、飞机机库基础上扩建而成，博物馆经近4年原址新建并扩充展品，于2012年北航甲子校庆更名并重新开馆，展区面积约8300平米，分为长空逐梦、银鹰巡空、神舟问天、空天走廊4个展区，馆藏300多件国内外公认的航空航天文物精品以及结构、发动机、机载设备等珍贵实物，通过高科技手段展示了航空航天原理以及人类飞天的历程。展馆集教学、科普、文化传承为一体，承担了一部分教学任务，例如《航空航天概论》课程以及航空发动机、飞机结构、起落架结构、导弹结构、航天器设计等核心专业课的教学实践任务。是航空航天国家级实验教学示范中心的重要组成部分，也是航空航天科普与文化、北航精神以及青少年爱国主义、国防教育的重要基地。\n",
                "https://s1.imagehub.cc/images/2023/12/04/1a9532a427da614b1e47fd52f4476442.jpeg"));
        tourUnits.add(new TourUnit("长空逐梦展区",
                "长空逐梦展区向公众展示北航先进的科研水平，航空设计、航空发动机、航空电子展品为核心展区。一个完整飞机造型贯穿该展厅，飞机分段展示了机头、机舱和机身的内容，配合地面机身投射的具有速度感的条纹状装饰。该展区包括大量解剖的飞机机身、机翼、起落架、发动机及机载设备等珍贵展品，实物展示与动态演示结合，是飞机设计核心专业课程的重要教学实践基地。\n",
                "长空逐梦展厅分四大展区：航空设计、航空发动机、航空电子、航空航天4大集团。长空逐梦展厅空间设计以一个完整飞机造型贯穿展厅。飞机的造型分段展示了机头、机舱和机身的内容，墙面采用蓝白相间的云层造型，配合地面机身投射的具有速度感的条纹状装饰，营造出一个飞机在云层中穿梭飞行的艺术空间环境，你可以感受到直观的视觉冲击力，体会展馆的主题思想。航空设计展区针对展示内容，采用开放式布局，实物展示结合多媒体互动、图文板全面的展陈内容，你可以自由规划参展路线，充分了解相关知识。航空电子展区以互动答题的形式，可以让你的参观更有趣味性体验。\n",
                "https://mmbiz.qpic.cn/mmbiz_jpg/hgBuAqn2p83EHDwC8SNB9iaBwXBiapfEoHKoiaHR7dVuJwiclAJ7xDqic3xvmYj4IPF1bhaceI2icbwjMZ191icoy2zGg/640?wx_fmt=jpeg&tp=wxpic&wxfrom=5&wx_lazy=1&wx_co=1"));
        tourUnits.add(new TourUnit("银鹰巡空展区",
                "银鹰巡空展区分两部分：北航特色飞机、实体飞机展示。核心区域为北航特色飞机展示，展示历年来北航自主设计制作的实体飞机展品，藏品包括“北京一号”、“黑寡妇”、“鹞”式垂直起降战斗机等。通过不断变化的空间设计表达了飞机发展的历史变迁，空间的转折启承变化体现了飞机发展的连续时空，向人们展示北航为航空事业做出的巨大贡献。\n",
                "银鹰巡空展区是整个航空馆的核心展区，陈列了北航自建校以来收藏的近三十架各型实体飞机。这些来自不同国家、不同时代、不同用途的飞机，讲述了一段段关于天空和飞翔的故事。它们有的立下赫赫战功，有的曾为人民服务。这些实体飞机藏品展示采用地面与空中立体式的展陈方式，极富有视觉冲击力。银翼巡空展厅分二大展区：北航特色飞机、实体飞机展示，通过不断变化的空间设计表达了飞机发展的历史变迁，你可以从空间的转折启承变化感受到飞机发展连续的时空体验。展厅中的“北京一号”是我国第一架自行设计和研制的轻型旅客机。它所展现的独立自主、自力更生、艰苦奋斗、知行合一、空天报国的理念，激励着一代又一代北航学子矢志向前，成为了北航学子心目中一座不朽的丰碑。\n",
                "https://mmbiz.qpic.cn/mmbiz_jpg/hgBuAqn2p83EHDwC8SNB9iaBwXBiapfEoHbA6T0Dj8ReB6Rl6ZyDaulEW0wLroaslljyaf1LBiayd4Mo8sCiaJKlMg/640?wx_fmt=jpeg&tp=wxpic&wxfrom=5&wx_lazy=1&wx_co=1"));
        tourUnits.add(new TourUnit("空天走廊展区",
                "空天走廊展区分两部分：航空发展史、航天发展史。按照历史发展的时间顺序，向人们展开了一幅航空航天发展的历史画卷。展区通过顶部悬吊的模型与地面的多媒体展柜相互呼应，向人们展示了航空航天发展史上的一个个里程碑。\n",
                "“飞天”是中华民族自古以来的梦想，而追梦则是北航人的使命与追求。空天走廊展区向我们展示了新中国航空航天事业的点点积累和巨大成就。新中国的航空工业从无到有，从小到大，从弱到强，从追逐跟跑到与世界大国同台竞技，跨越式发展的背后，是一代代航空航天人的辛苦付出。\n",
                "https://mmbiz.qpic.cn/mmbiz_jpg/hgBuAqn2p83EHDwC8SNB9iaBwXBiapfEoH9kKF1lCGo7icgylibZpTtg0J6yo7Kxiav59k3yRwibicIILErGSyLQS2DXw/640?wx_fmt=jpeg&tp=wxpic&wxfrom=5&wx_lazy=1&wx_co=1"));
        tourUnits.add(new TourUnit("神舟问天展区",
                "神舟问天展区分五部分：火箭与导弹、卫星与探测器、载人航天、航空航天互动区、学生作品区。展区环境设计结合航天主题，整个空间力图打造成一个充满科幻色彩的太空试验基地的视觉效果，立面材质采用冷峻的、代表理性科技的灰色金属材料，背景墙面统一采用深邃的太空背景画，加上深色的天花背景相配合，置身其中产生一种时空错觉感，有一种身临其境的沉浸感。\n",
                "这一展区主要介绍航天相关展品，同时也有部分导弹展品。1970年4月24日，东方红一号卫星在酒泉卫星发射中心发射升空，使我国成为了世界上第五个可以独立发射人造地球卫星的国家。1958年9月22日，北京二号发射，北京二号高6米，是北航自主设计研发的，中国第一枚高空探测火箭，主要设计用于气象和高空探测。“北京二号”是中国，也是亚洲第一枚液、固两种推进剂的近代两级探空火箭。作为北航当年响应国家号召规划的“十大工程”之一，“北京二号”得到了周恩来总理等老一代国家领导人的高度肯定，并与“北京一号”轻型客机、“北京五号”无人驾驶飞机共同作为标志性成果，载入了北航和中国航空航天事业的史册。更为重要的是，“北京二号”凝聚了北航人空天报国的爱国情怀，也凝聚了北航人敢为人先的卓越追求。\n",
                "https://mmbiz.qpic.cn/mmbiz_jpg/hgBuAqn2p83EHDwC8SNB9iaBwXBiapfEoHOB43Q3moSFczaiceSFics0Rs69rYcrTiaYAfAoms00aicicEcjOjFGVG8uA/640?wx_fmt=jpeg&tp=wxpic&wxfrom=5&wx_lazy=1&wx_co=1"));
        tourMap.put("北京航空航天博物馆", tourUnits);
        tourUnits = new ArrayList<TourUnit>();
        tourUnits.add(new TourUnit("总馆",
                "北京航空航天大学校史馆是一座展示北航六十多年办学历程和成就的文化场馆，位于北航学院路校区内，占地面积约5000平方米，分为五层，分别展示了北航的创业篇、奋进篇、改革篇、创新篇和艺术馆。校史馆以文字、图片、实物、多媒体等多种形式，全面反映了北航人的爱国奉献、航空救国、空天报国的理想和精神，以及北航在人才培养、科技创新、社会服务、文化传承等方面的突出贡献和发展足迹。校史馆是北航的文化记忆、传承与创新的重要载体，也是北航的文化名片和精神家园。\n",
                "北京航空航天大学校史馆是一座展示北航历史、文化和科技成果的精品展馆，位于北航学院路校区内，紧邻北航的航空航天博物馆和晨兴音乐厅。校史馆于2012年正式对外开放，是北航文化记忆、传承与创新的重要载体。\n" +
                        "校史馆展厅总面积近4500平方米，分为五层。一层为科技成果展，展示了北航在航空航天、国防科技、信息技术等领域的重大突破和创新。二至四层为校史展，按照“空天报国”主题，分为“创校之始，与子念之”、“空天报国，为国育才”、“改革开放，创新发展”、“面向未来，追求卓越”四个部分，展示了北航从1952年成立至今的发展历程和辉煌成就，以及北航人的奋斗精神和爱国情怀。五层为艺术馆，展示了北航师生校友的艺术作品，体现了北航人的文化素养和审美情趣。\n" +
                        "校史馆不仅是一座展馆，也是一座教育基地，是北航人学习校史、弘扬校风、传承校魂的重要场所。校史馆欢迎广大师生校友和社会各界人士参观，感受北航的历史魅力和现代风采。\n",
                "https://s1.imagehub.cc/images/2023/12/04/1a9532a427da614b1e47fd52f4476442.jpeg"));
        tourUnits.add(new TourUnit("科技成果展",
                "这是一层的展览，展示了北航近十年来在航空航天、信息技术、生命健康等领域的科技创新成果，包括国际领先的神经外科手术机器人、高端自旋芯片、无人机、飞行器虚拟现实技术等，反映了北航服务国家战略需求和经济社会发展的能力和贡献。\n",
                "北航是一所以航空航天为特色，多学科协调发展的高等学府，其科技成果展展示了北航在各个领域的创新能力和社会贡献。在这里，你可以欣赏到北航人参与的国家重大工程项目的模型、原型和实物，如神舟飞船、嫦娥探月、北斗导航、歼-20战斗机、C919大型客机等，感受到北航人的空天梦想和报国情怀。你还可以了解到北航人在信息技术、新材料、新能源等领域的突破性的研究成果，如量子通信、超导材料、石墨烯、太阳能电池等，体验到北航人的求实创新和科技情怀。\n",
                "https://s1.imagehub.cc/images/2023/12/04/b596a7023db9139ef6552bd21d2034c6.jpeg"));
        tourUnits.add(new TourUnit("校史展-创业篇",
                "这是二层的展览，回顾了北航的创校历程和初期发展，展示了北航的前身——八所院校航空系的办学情况，以及北航的创始人和第一代教师的风采，体现了北航人的爱国奉献和航空救国的理想。\n",
                "创业篇展示了北航从成立至1966年的发展历程，以及北航人在航空航天事业中的贡献和风采。在这里，你可以亲眼看到北航的创始人、老领导、老专家、老教授等的珍贵实物和资料，如北航的第一任校长钱学森的办公桌、北航的第一批教师的合影、北航的第一批毕业生的证书等，感受到北航人的创业精神和空天报国的理想和使命。你还可以了解到北航在创业时期的办学特色和成就，如北航的第一台风洞、第一架飞机、第一颗人造卫星、第一颗原子弹等，体验到北航人的奋斗历程和辉煌成果。\n",
                "https://s1.imagehub.cc/images/2023/12/04/78c7eeeab71edb028c77c7cb7683593f.jpeg"));
        tourUnits.add(new TourUnit("校史展-奋进篇",
                "这是三层的展览，展示了北航在1952年至1978年间的发展历程，展现了北航人在艰苦环境下团结拼搏、自力更生、勇于创新的精神，以及北航在人才培养、科学研究、社会服务等方面为国家航空航天事业做出的突出贡献。\n",
                "奋进篇展示了北航人在这一时期的坚韧不拔和贡献和风采。在这里，你可以了解到北航在文化大革命期间的迁徙历程、北航的抗震救灾、北航的知识青年上山下乡等，感受到北航人的艰苦奋斗和无私奉献。你还可以了解到北航在奋进时期的办学特色和成就，如北航的第一台计算机、第一颗氢弹、第一次载人飞行、第一次空间对接等，体验到北航人的求实创新和奋进精神。\n",
                "https://s1.imagehub.cc/images/2023/12/04/27ec608b02b4f026f8a1177e1efd60af.jpeg"));
        tourUnits.add(new TourUnit("校史展-改革篇",
                "这是四层的展览，展示了北航在改革开放以来的发展历程，展现了北航人在改革创新中的担当实干和敢为人先的精神，以及北航在教育体制改革、学科建设、科技创新、国际合作等方面取得的显著成就。\n",
                "改革篇展示了北航人在这一时期的开拓创新和贡献和风采。在这里，你可以了解到北航在改革开放期间的教育改革、科研改革、管理改革、校园建设等，感受到北航人的改革精神和开放包容的视野和思想。你还可以了解到北航在改革时期的办学特色和成就，如北航的第一批博士生、第一批留学生、第一批海外合作项目、第一批高新技术企业等，体验到北航人的创新能力和国际化水平。\n",
                "https://s1.imagehub.cc/images/2023/12/04/44e462e5e4906881d9f717fc15ffc104.jpeg"));
        tourUnits.add(new TourUnit("校史展-创新篇",
                "这是四层的展览，展示了北航在新世纪以来的发展历程，展现了北航人在追求一流中的创新创造和开拓进取的精神，以及北航在服务国家战略需求、建设世界一流大学、培养高素质人才等方面的发展理念和使命担当。\n",
                "创新篇展示了北航人在这一时期的前瞻领先和贡献和风采。在这里，你可以了解到北航的“双一流”建设、国际化发展、社会服务、文化建设等，感受到北航人的创新精神和空天梦想的不断追求。你还可以了解到北航在创新时期的办学特色和成就，如北航的第一颗北斗导航卫星、第一次载人航天飞行、第一次月球探测、第一次火星探测等，体验到北航人的前瞻领先的理念和水平。\n",
                "https://s1.imagehub.cc/images/2023/12/04/8da04c06015b8ba3c5be70882018663a.jpeg"));
        tourUnits.add(new TourUnit("艺术馆",
                "这是五层的展览，展示了北航的艺术教育和艺术创作的成果，包括书画、摄影、雕塑、陶艺、剪纸、民族乐器等，反映了北航人的人文素养和艺术情趣，展现了北航的文化魅力和创新活力。\n",
                "北航不仅是一所科技型的高等学府，也是一所人文型的高等学府，其艺术馆展示了北航的艺术教育和艺术创作，包括书法、绘画、摄影、雕塑、陶艺、剪纸等多种形式的艺术作品，体现了北航的人文气息和艺术魅力。在这里，你可以欣赏到北航的艺术教育的历史和现状，如北航的美术教育、音乐教育、舞蹈教育、戏剧教育等，感受到北航人的审美情趣和创造力。你还可以欣赏到北航的艺术创作的风格和特色，如北航的书法家、画家、摄影家、雕塑家、陶艺家、剪纸家等，体验到北航人的艺术精神和创新能力。\n",
                "https://s1.imagehub.cc/images/2023/12/04/0e187b6ca94490e1f9f5f1c77933683a.jpeg"));
        tourMap.put("北航校史馆", tourUnits);
        //更新标题
        setTitles();
    }

}