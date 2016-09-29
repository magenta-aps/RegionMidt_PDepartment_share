//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package dk.magenta.rm;


import org.alfresco.web.cmm.CMMService;
import org.alfresco.web.cmm.CMMServiceGet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CustomCMMService extends CMMService {
    private static final Log logger = LogFactory.getLog(CMMServiceGet.class);

    public CustomCMMService() {

    }

    public String  getForm(String model, String type) {
        String t = (String)this.getFormDefinitions(model).get(type);

        return t;
    }


}
