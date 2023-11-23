package com.example.navbar.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navbar.util.Msg;
import com.example.navbar.util.MsgAdapter;
import com.example.navbar.R;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {


    private static final String TAG = "ChatFragment";
    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private EditText inputText;
    private Button send;
    private LinearLayoutManager layoutManager;
    private MsgAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        msgRecyclerView = view.findViewById(R.id.msg_recycler_view);
        inputText = view.findViewById(R.id.input_text);
        send = view.findViewById(R.id.send);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new MsgAdapter(msgList = getData());

        msgRecyclerView.setLayoutManager(layoutManager);
        msgRecyclerView.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!content.equals("")) {
                    msgList.add(new Msg(content, Msg.TYPE_SEND));
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    inputText.setText("");
                }

                // Your custom one-question-one-answer logic here
                // ...

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