package com.checkedin.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.checkedin.MainActivity;
import com.checkedin.R;
import com.checkedin.custom.CustomFragment;
import com.checkedin.model.Conversation;
import com.example.ibrahim.myapplication.backend.messaging.Messaging;
import com.example.ibrahim.myapplication.backend.registration.Registration;
import com.example.ibrahim.myapplication.backend.registration.model.RegistrationRecord;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * The Class Chat is the Fragment class that is launched when the user clicks
 * any of item shown in Right navigation panel of Main Activity.. The current
 * implementation simply shows dummy conversations and when you send a chat
 * message it will show a dummy auto reply message. You can write your own code
 * for actual Chat.
 */
public class Chat extends CustomFragment {
    //The Conversation list.
    public static ArrayList<Conversation> convList = new ArrayList<>();

    //The chat adapter.
    public static ChatAdapter adp;

    private EditText txt;
    String user_name;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_chat, null);
        user_name = getArguments().getString("username");
        ((MainActivity)getActivity()).setTitle(user_name);
        ListView list = (ListView) v.findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) v.findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        setTouchNClick(v.findViewById(R.id.btnSend));
        return v;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btnSend) {
            sendMessage();
        }
    }

    /**
     * Call this method to Send message to opponent. The current implementation
     * simply add an auto reply message with each sent message. You need to
     * write actual logic for sending and receiving the messages.
     */
    private void sendMessage() {
        if (txt.length() == 0)
            return;
        ChatAsyncTask task = new ChatAsyncTask();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

        String s = txt.getText().toString();
        String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new java.util.Date());
        task.execute(new Pair<>(user_name, s));
        convList.add(new Conversation(s, timeStamp, true));
        adp.notifyDataSetChanged();
        txt.setText(null);
    }

    /**
     * The Class CutsomAdapter is the adapter class for Chat ListView. The
     * currently implementation of this adapter simply display static dummy
     * contents. You need to write the code for displaying actual contents.
     */
    private class ChatAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return convList.size();
        }

        @Override
        public Conversation getItem(int arg0) {
            return convList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int pos, View v, ViewGroup arg2) {
            Conversation c = getItem(pos);
            if (c.isSent())
                v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_chat_item_sent, null);
            else
                v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_chat_item_rcv, null);
            TextView lbl = (TextView) v.findViewById(R.id.lbl1);
            lbl.setText(c.getMsg());
            return v;
        }
    }

    public static class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String timeStamp = new SimpleDateFormat("HH.mm.ss").format(new java.util.Date());
            convList.add(new Conversation(message, timeStamp, false));
            adp.notifyDataSetChanged();
        }
    }

    private  class ChatAsyncTask extends AsyncTask<Pair<String, String>, Void, String> {
        private Messaging messaging = null;
        private Registration registration = null;

        @Override
        protected String doInBackground(Pair<String, String>... params) {
            if (registration == null) {  // Only do this once
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://2-dot-gleaming-abacus-123320.appspot.com/_ah/api")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                registration = builder.build();
            }
            if (messaging == null) {  // Only do this once
                Messaging.Builder builder = new Messaging.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl("https://2-dot-gleaming-abacus-123320.appspot.com/_ah/api")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                messaging = builder.build();
            }
            RegistrationRecord regId = null;
            String message = null;
            while (true) {
                try {
                    String username = params[0].first;
                    regId = registration.getRegistrationID(username).execute();
                    message = params[0].second;
                    messaging.sendMessageTo(message, regId.getRegId()).execute();
                    break;
                } catch (IOException e) {
                }
            }
            return message;
        }
    }
}