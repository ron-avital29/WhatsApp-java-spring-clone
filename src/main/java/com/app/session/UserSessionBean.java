package com.app.session;

import com.app.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Getter
@Component
@SessionScope
public class UserSessionBean {

    @Setter
    private boolean loggedIn;
    private Deque<Long> recentChatrooms = new LinkedList<>();
    @Setter
    private User user;

    @PostConstruct
    public void init() {
        loggedIn = false;
    }

    public void visitChatroom(Long chatroomId) {
        recentChatrooms.remove(chatroomId);
        recentChatrooms.addFirst(chatroomId);
        if (recentChatrooms.size() > 10) {
            recentChatrooms.removeLast();
        }
    }

    public List<Long> getRecentChatrooms() {
        return new ArrayList<>(recentChatrooms);
    }
}

