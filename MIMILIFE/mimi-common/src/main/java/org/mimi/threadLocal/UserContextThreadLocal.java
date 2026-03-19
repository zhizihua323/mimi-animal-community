package org.mimi.threadLocal;

import org.mimi.entity.User;

public class UserContextThreadLocal {
    public static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(User user){
        USER_HOLDER.set(user);
    }

    public static User getUser(){
        return USER_HOLDER.get();
    }

    public static void  clear(){
        USER_HOLDER.remove();
    }


}
