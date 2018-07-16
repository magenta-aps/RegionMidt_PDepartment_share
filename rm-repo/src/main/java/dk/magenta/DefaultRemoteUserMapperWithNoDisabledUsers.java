package dk.magenta;

import org.alfresco.repo.security.authentication.external.DefaultRemoteUserMapper;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by seth on 09/06/16. Modified by Alexander on 16/07/18.
 */
public class DefaultRemoteUserMapperWithNoDisabledUsers extends DefaultRemoteUserMapper {

    private PersonService personService;

    private static Log logger = LogFactory.getLog(DefaultRemoteUserMapperWithNoDisabledUsers.class);

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public String getRemoteUser(HttpServletRequest request) {

        String remoteUser = super.getRemoteUser(request);

        if (remoteUser == null) {
            logger.debug("UserName was not set.");
            return null;
        }

        // Don't allow disabled users to login.
        if (isUserEnabled(remoteUser)) {
            logger.debug("UserName was set to " + remoteUser + ".");
            logger.warn("A disabled user (" + remoteUser + ") has tried to log on.");
            return remoteUser;
        }
        else {
            return null;
        }
    }

    private boolean isUserEnabled(final String userName) {
        // Return false if the service is not set
        if(personService == null) {
            logger.error("PersonService is null.");
            return false;
        }

        // Return true if the person exists and is enabled.
        if(!personService.personExists(userName))
        {
            logger.warn("A non-existent user (" + userName + ") tried to log on.");
            return false;
        }
        else if(!personService.isEnabled(userName))
        {
            logger.warn("A disabled user (" + userName + ") has tried to log on.");
            return false;
        }
        else return true;
    }
}
