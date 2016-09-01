package dk.magenta.rm.scripts;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

interface PresetGlobal {
    String DATA_DICTIONARY_QUERY_PATH = "+PATH:\"app:company_home/app:dictionary";
    String EXTENSION_FOLDER_NAME = "Extension Presets";
    String EXTENSION_FOLDER_NAME_SMALL = EXTENSION_FOLDER_NAME.toLowerCase().replace(" ", "");
    String EXTENSION_FOLDER_ID = "cm:" + EXTENSION_FOLDER_NAME_SMALL;
    QName EXTENSION_FOLDER_ID_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, EXTENSION_FOLDER_NAME_SMALL);
}
