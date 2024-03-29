package com.martabak.kamar.service;

import android.content.Context;

import com.martabak.kamar.domain.chat.ChatMessage;
import com.martabak.kamar.domain.chat.GuestChat;
import com.martabak.kamar.service.response.PostResponse;
import com.martabak.kamar.service.response.PutResponse;
import com.martabak.kamar.service.response.ViewResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Exposes {@link ChatService}.
 */
public class ChatServer extends Server {

    /**
     * The singleton instance.
     */
    private static ChatServer instance;

    /**
     * The service api conf.
     */
    private ChatService service;

    /**
     * Constructor.
     */
    private ChatServer(Context c) {
        super(c);
        service = createService(ChatService.class);
    }

    /**
     * Obtains singleton instance.
     * @return The singleton instance.
     */
    public static ChatServer getInstance(Context c) {
        if (instance == null)
            instance = new ChatServer(c);
        return instance;
    }

    /**
     * Send a chat message.
     * @param message The chat message to be sent.
     * @return Whether or not the chat message was successfully sent.
     */
    public Observable<Boolean> sendChatMessage(ChatMessage message) {
        return service.sendChatMessage(message)
                .map(new Func1<PostResponse, Boolean>() {
                    @Override public Boolean call(PostResponse response) {
                        return response.ok;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Get the full chat log for a particular guest.
     * @param guestId The guest's ID.
     * @return The guest chat.
     */
    public Observable<GuestChat> getGuestChat(String guestId) {
        return service.getGuestChat('"' + guestId + '"')
                .flatMap(new Func1<ViewResponse<ChatMessage>, Observable<GuestChat>>() {
                    @Override public Observable<GuestChat> call(ViewResponse<ChatMessage> response) {
                        List<ChatMessage> messages = new ArrayList<>(response.total_rows);
                        for (ViewResponse<ChatMessage>.ViewResult<ChatMessage> i : response.rows) {
                            messages.add(i.value);
                        }
                        Collections.sort(messages, new Comparator<ChatMessage>() {
                            @Override
                            public int compare(ChatMessage m1, ChatMessage m2) {
                                return m1.sent.compareTo(m2.sent);
                            }
                        });
                        return Observable.just(new GuestChat(messages));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Update a chat message.
     * @param message The chat message to be sent.
     * @return Whether or not the chat message was successfully sent.
     */
    public Observable<Boolean> setChatMessageToRead(ChatMessage message) {
        return service.updateChatMessage(message._id, new ChatMessage(
                        message._id,
                        message._rev,
                        message.guestId,
                        message.from,
                        message.message,
                        message.sent,
                        new Date())
                )
                .map(new Func1<PutResponse, Boolean>() {
                    @Override public Boolean call(PutResponse response) {
                        return response.ok;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
