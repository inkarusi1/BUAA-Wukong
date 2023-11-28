package com.example.navbar.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navbar.util.AsrThread;
import com.example.navbar.util.Msg;
import com.example.navbar.util.MsgAdapter;
import com.example.navbar.R;
import com.example.navbar.util.OkHttpUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {


    private static final String TAG = "ChatFragment";
    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private Button voice;
    private EditText inputText;
    private Button send;
    private LinearLayoutManager layoutManager;
    private MsgAdapter adapter;
    private boolean speaking;
    private AsrThread asrThread;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        msgRecyclerView = view.findViewById(R.id.msg_recycler_view);
        voice = view.findViewById(R.id.voice);
        inputText = view.findViewById(R.id.input_text);
        send = view.findViewById(R.id.send);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MsgAdapter(msgList = getData());
        speaking = false;

        msgRecyclerView.setLayoutManager(layoutManager);
        msgRecyclerView.setAdapter(adapter);

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!speaking) {
                    Toast.makeText(getContext(), "开始录音", Toast.LENGTH_LONG).show();
                    asrThread = new AsrThread(requireContext(), inputText);
                    asrThread.start();
                    speaking = true;
                } else {
                    asrThread.endThread();
                    String result = AsrThread.getResult();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            inputText.setText(AsrThread.getResult());
                            Log.i(TAG, "xxxxxxxxxx ChatFragment: [" + AsrThread.getResult() + "]");
                        }
                    }, 3000);
//                    inputText.setText(result);
                    speaking = false;
                    Toast.makeText(getContext(), "录音结束", Toast.LENGTH_LONG).show();
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!OkHttpUtils.getInstance().isResponding()) {
                    String content = inputText.getText().toString();
                    if (!content.equals("")) {
                        msgList.add(new Msg(content, Msg.TYPE_SEND));
                        adapter.notifyItemInserted(msgList.size() - 1);
                        msgRecyclerView.scrollToPosition(msgList.size() - 1);
                        inputText.setText("");

                        // 获取人工智能回复
//                        OkHttpUtils.getInstance().callResponseOf(content, ChatFragment.this);
                        String hello = "hello, world";
                        inputText.setText(hello);
                    }

                    // Your custom one-question-one-answer logic here
                    // ...
                } else {
                    Toast.makeText(getContext(), "我还在回答中哦", Toast.LENGTH_LONG).show();
                }

            }
        });

        return view;
    }

    private List<Msg> getData() {
        List<Msg> list = new ArrayList<>();
        list.add(new Msg("Hello", Msg.TYPE_RECEIVED));
        return list;
    }

    //添加send消息
    public void addSendMsg(String content) {
        msgList.add(new Msg(content, Msg.TYPE_SEND));
        adapter.notifyItemInserted(msgList.size() - 1);
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
    }

    //添加receive消息
    public void addReceiveMsg(String content) {
        msgList.add(new Msg(content, Msg.TYPE_RECEIVED));
        adapter.notifyItemInserted(msgList.size() - 1);
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
    }

    //清空消息
    public void clearMsg() {
        msgList.clear();
        adapter.notifyDataSetChanged();
    }

}