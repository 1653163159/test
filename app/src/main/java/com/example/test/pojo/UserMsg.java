package com.example.test.pojo;

/**
 * @author : hqx
 * @date : 6/2/2023 上午 11:31
 * @descriptions: 保存用户消息
 */
public class UserMsg {
    private String jid;
    private String user;
    private String msg;
    private String flag;

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
