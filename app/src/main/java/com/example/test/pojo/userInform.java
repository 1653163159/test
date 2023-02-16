package com.example.test.pojo;

import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;

/**
 * @author : hqx
 * @date : 2/2/2023 上午 11:11
 * @descriptions: openfire服务器用户信息
 */
public class userInform {

    private String jid;
    private String name;

    public userInform(String jid, String name) {
        this.jid = jid;
        this.name = name;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
