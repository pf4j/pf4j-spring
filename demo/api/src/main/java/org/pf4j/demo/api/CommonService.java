package org.pf4j.demo.api;
/**
 * Provide public service support, such as sending mail
 * @author : Ted
 * @version : 1.0.0
 * @date : 2021/6/22  13:30
 */
public interface CommonService {


    /** Desc: <br>
     * 〈Send Email〉
     *
     * @param address
     * @param sender

     * @return: java.lang.Boolean
     * @since : 1.0.0
     * @author : Ted
     * @date : 2021/6/22 13:32
     */
    public Boolean sendEmail(String address, String sender);
}
