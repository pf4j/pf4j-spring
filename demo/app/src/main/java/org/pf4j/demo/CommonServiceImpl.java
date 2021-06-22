package org.pf4j.demo;

import org.pf4j.demo.api.CommonService;

/**
 * Provide public service support, such as sending mail
 * @author : ChengYu.lyc
 * @version : 1.0.0
 * @date : 2021/6/22  13:33
 */
public class CommonServiceImpl implements CommonService {
    /**
     * Desc: <br>
     * 〈Send email common implementation〉
     *
     * @param address
     * @param sender
     * @return: java.lang.Boolean
     * @author : Ted
     * @date : 2021/6/22 13:32
     * @since : 1.0.0
     */
    @Override
    public Boolean sendEmail(String address, String sender) {
        System.out.println("The email send success,sender:"+sender+",the receiver:" + address);
        return Boolean.TRUE;
    }
}
