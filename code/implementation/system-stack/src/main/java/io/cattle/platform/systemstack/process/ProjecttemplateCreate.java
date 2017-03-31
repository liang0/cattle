package io.cattle.platform.systemstack.process;

import static io.cattle.platform.core.model.tables.AccountTable.*;

import io.cattle.platform.core.constants.AccountConstants;
import io.cattle.platform.core.model.Account;
import io.cattle.platform.core.model.ProjectTemplate;
import io.cattle.platform.engine.handler.HandlerResult;
import io.cattle.platform.engine.process.ProcessInstance;
import io.cattle.platform.engine.process.ProcessState;
import io.cattle.platform.object.meta.ObjectMetaDataManager;
import io.cattle.platform.process.base.AbstractDefaultProcessHandler;
import io.cattle.platform.sample.data.SampleDataStartupV3;
import io.cattle.platform.systemstack.catalog.CatalogService;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ProjecttemplateCreate extends AbstractDefaultProcessHandler {

    @Inject
    SystemStackTrigger systemStackTrigger;
    @Inject
    SampleDataStartupV3 sampleDataStartupV3;

    @Override
    public HandlerResult handle(ProcessState state, ProcessInstance process) {
        ProjectTemplate template = (ProjectTemplate)state.getResource();
        if (!Boolean.TRUE.equals(template.getIsPublic()) ||
                !CatalogService.DEFAULT_TEMPLATE.get().equalsIgnoreCase(template.getName())) {
            return null;
        }

        sampleDataStartupV3.start();
        Account defaultProject = getDefaultProject();
        if (defaultProject == null || defaultProject.getProjectTemplateId() != null) {
            return null;
        }

        if (!AccountConstants.ACCOUNT_VERSION.get().equals(defaultProject.getVersion())) {
            return null;
        }

        Long projectTemplateId = defaultProject.getProjectTemplateId();
        if (projectTemplateId == null) {
            projectTemplateId = template.getId();
        }

        systemStackTrigger.trigger(defaultProject.getId());
        objectManager.setFields(defaultProject,
                ACCOUNT.PROJECT_TEMPLATE_ID, projectTemplateId);

        return null;
    }

    protected Account getDefaultProject() {
        return objectManager.findAny(Account.class,
                ObjectMetaDataManager.UUID_FIELD, "adminProject");
    }

}