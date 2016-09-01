package dk.magenta;

import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.AuthenticationDao;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authentication.external.DefaultRemoteUserMapper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by seth on 09/06/16.
 */
public class DefaultRemoteUserMapperWithNoDisabledUsers extends DefaultRemoteUserMapper {
    private MutableAuthenticationDao authenticationDao;

    @Override
    public String getRemoteUser(HttpServletRequest request) {
        String remoteUser = super.getRemoteUser(request);
        if (remoteUser == null) {
            return null;
        }
        if (isUserEnabled(remoteUser)) {
            return remoteUser;
        } else {
            // Don't allow disabled users to login.
            return null;
        }
    }

    public boolean isUserEnabled(final String userName) {
        UserDetails userDetails = authenticationDao.loadUserByUsername(userName);
        return userDetails != null && userDetails.isEnabled();
    }

    public void setAuthenticationDao(MutableAuthenticationDao authenticationDao) {
        this.authenticationDao = authenticationDao;
    }
}
