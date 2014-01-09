/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.ui.widgets.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.DateUtil;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;
import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenericVstack;
import fr.pfgen.axiom.shared.records.StudyRecord;

/**
 *
 * @author eric
 */
public class StudyInfoTab extends Tab {

    private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);
    private String studyName;
    private VLayout tabPane;

    public StudyInfoTab(String name) {
        this.studyName = name;
        tabPane = new VLayout(10);
        tabPane.setDefaultLayoutAlign(Alignment.LEFT);

        setPrompt("Infos");
        setTitle("&nbsp;" + Canvas.imgHTML("icons/17.png", 16, 16));


        addTabSelectedHandler(new TabSelectedHandler() {
            @Override
            public void onTabSelected(TabSelectedEvent event) {
                for (Canvas c : tabPane.getMembers()) {
                    c.destroy();
                }
                tabPane.addMember(constructInfoLayout());
            }
        });

        setPane(tabPane);
    }

    private VStack constructInfoLayout() {
        final GenericVstack stack = new GenericVstack();
        stack.addHeaderLabel("Study informations");

        final HLayout nameLayout = new HLayout(2);
        nameLayout.setAutoHeight();
        nameLayout.setAutoHeight();
        Label nameLabel = new Label();
        nameLabel.setContents("Study&nbsp;name:&nbsp;");
        nameLabel.setAutoHeight();
        nameLabel.setAutoWidth();
        Label studyNameLabel = new Label();
        studyNameLabel.setContents(studyName);
        studyNameLabel.setAutoHeight();
        studyNameLabel.setAutoWidth();

        nameLayout.addMember(nameLabel);
        nameLayout.addMember(studyNameLabel);
        
        final HLayout typeLayout = new HLayout(2);
        typeLayout.setAutoHeight();
        typeLayout.setAutoWidth();
        Label typeLabel = new Label();
        typeLabel.setContents("Type:&nbsp;");
        typeLabel.setAutoHeight();
        typeLabel.setAutoWidth();
        final Label studyTypeLabel = new Label();
        studyTypeLabel.setAutoHeight();
        studyTypeLabel.setAutoWidth();
        
        typeLayout.addMember(typeLabel);
        typeLayout.addMember(studyTypeLabel);

        final HLayout dateLayout = new HLayout(2);
        dateLayout.setAutoHeight();
        dateLayout.setAutoHeight();
        Label dateLabel = new Label();
        dateLabel.setContents("Date&nbsp;of&nbsp;creation:&nbsp;");
        dateLabel.setAutoHeight();
        dateLabel.setAutoWidth();
        final Label studyDateLabel = new Label();
        studyDateLabel.setAutoHeight();
        studyDateLabel.setAutoWidth();

        dateLayout.addMember(dateLabel);
        dateLayout.addMember(studyDateLabel);

        final HLayout createdByLayout = new HLayout();
        createdByLayout.setAutoHeight();
        createdByLayout.setAutoWidth();
        Label createdByLabel = new Label();
        createdByLabel.setContents("Created&nbsp;by:&nbsp;");
        createdByLabel.setAutoHeight();
        createdByLabel.setAutoWidth();
        final Label studyCreatedByLabel = new Label();
        studyCreatedByLabel.setAutoHeight();
        studyCreatedByLabel.setAutoWidth();

        createdByLayout.addMember(createdByLabel);
        createdByLayout.addMember(studyCreatedByLabel);

        final HLayout descriptionLayout = new HLayout(2);
        descriptionLayout.setAutoHeight();
        descriptionLayout.setAutoWidth();
        Label descriptionLabel = new Label();
        descriptionLabel.setContents("Description:&nbsp;");
        descriptionLabel.setAutoHeight();
        descriptionLabel.setAutoWidth();
        final HTMLFlow descriptionPane = new HTMLFlow();
        descriptionPane.setShowEdges(false);

        
        descriptionLayout.addMember(descriptionLabel);
        descriptionLayout.addMember(descriptionPane);

        studiesService.getStudyInfos(studyName, new AsyncCallback<StudyRecord>() {
            @Override
            public void onFailure(Throwable caught) {
                SC.warn("Cannot fetch study informations from server !");
            }

            @Override
            public void onSuccess(StudyRecord result) {
                studyTypeLabel.setContents(result.getStudyType());
                studyDateLabel.setContents(DateUtil.formatAsShortDate(result.getCreated()));
                studyCreatedByLabel.setContents(result.getCreatedBy());
                descriptionPane.setContents(result.getDescription());
            }
        });

        stack.addMember(nameLayout);
        stack.addMember(typeLayout);
        stack.addMember(dateLayout);
        stack.addMember(createdByLayout);
        stack.addMember(descriptionLayout);

        return stack;
    }
}
