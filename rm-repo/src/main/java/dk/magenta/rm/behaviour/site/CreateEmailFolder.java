package dk.magenta.rm.behaviour.site;

import org.alfresco.email.server.EmailServerModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.PolicyComponent;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceImpl;

import org.alfresco.service.transaction.TransactionService;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.model.ContentModel.*;

public class CreateEmailFolder implements NodeServicePolicies.OnCreateNodePolicy {

    private static Logger logger = Logger.getLogger(CreateEmailFolder.class);

    // Dependencies
    private NodeService nodeService;
    private SiteService siteService;
    private TransactionService transactionService;
    private TaggingService taggingService;
    private PolicyComponent policyComponent;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void init() {

        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    }

    public void onCreateNode(ChildAssociationRef childAssocRef) {

        NodeRef siteRef = childAssocRef.getChildRef();

        // Do not execute behaviour if this has been created in another store than workspace
        if (!siteRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
            // This is not the spaces store - probably the archive store
            return;
        }

        // Only add inbox if the site has been created
        if (nodeService.exists(siteRef)) {
            // Document Library for sites are created on first time a user enters it
            // Creating Document Library as SiteContainer to the new site
            SiteInfo siteInfo = siteService.getSite(siteRef);
            String siteName = siteInfo.getShortName();
            NodeRef documentLibrary = SiteServiceImpl.getSiteContainer(
                    siteName,
                    SiteService.DOCUMENT_LIBRARY,
                    true,
                    siteService,
                    transactionService,
                    taggingService);

            // Checks which folders have already been created
            List<String> existingFolders = new ArrayList<>();
            List<ChildAssociationRef> folderChildren = nodeService.getChildAssocs(documentLibrary);
            for (ChildAssociationRef folderChild:folderChildren) {
                NodeRef child = folderChild.getChildRef();
                existingFolders.add(nodeService.getProperty(child, PROP_NAME).toString());
            }

            String folderName = "Inbox";
            // Only add folder if it does not already exist
            if(!existingFolders.contains(folderName)) {
                // Add new folder called "Inbox" as child to the newly created site
                Map<QName, Serializable> inboxProperties = new HashMap<QName, Serializable>();
                inboxProperties.put(PROP_NAME, folderName);
                NodeRef inbox = nodeService.createNode(documentLibrary, ASSOC_CONTAINS, QName.createQName("cm:inbox"), TYPE_FOLDER, inboxProperties).getChildRef();

                // Add aspect emailserver:aliasable for folder Inbox
                Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
                aspectProperties.put(EmailServerModel.PROP_ALIAS, siteName);
                nodeService.addAspect(inbox, EmailServerModel.ASPECT_ALIASABLE, aspectProperties);
            }
        }
    }
}

