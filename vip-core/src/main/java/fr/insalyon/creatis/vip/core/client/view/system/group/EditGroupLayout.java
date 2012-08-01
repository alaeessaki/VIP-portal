/* Copyright CNRS-CREATIS
 *
 * Rafael Ferreira da Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.rafaelsilva.com
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.core.client.view.system.group;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.rpc.ConfigurationService;
import fr.insalyon.creatis.vip.core.client.rpc.ConfigurationServiceAsync;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.ModalWindow;
import fr.insalyon.creatis.vip.core.client.view.common.AbstractFormLayout;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.core.client.view.system.user.UserRecord;
import fr.insalyon.creatis.vip.core.client.view.util.FieldUtil;
import fr.insalyon.creatis.vip.core.client.view.util.WidgetUtil;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael Ferreira da Silva
 */
public class EditGroupLayout extends AbstractFormLayout {

    private String oldName = null;
    private boolean newGroup = true;
    private TextItem nameItem;
    private CheckboxItem isPublicField;
    private IButton removeButton;
    private ListGrid grid;
    private ModalWindow gridModal;
    private HLayout rollOverCanvas;
    private ListGridRecord rollOverRecord;

    public EditGroupLayout() {

        super(380, 450);
        addTitle("Add/Edit Group", CoreConstants.ICON_GROUP);

        configure();
        loadUsers();
    }

    private void configure() {

        nameItem = FieldUtil.getTextItem(350, null);

        isPublicField = new CheckboxItem();
        isPublicField.setTitle("Public");
        isPublicField.setWidth(350);

        IButton saveButton = new IButton("Save", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (nameItem.validate()) {
                    save(nameItem.getValueAsString().trim(),
                            isPublicField.getValueAsBoolean());
                }
            }
        });
        saveButton.setIcon(CoreConstants.ICON_SAVE);

        removeButton = new IButton("Remove", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (nameItem.validate()) {
                    remove(nameItem.getValueAsString().trim());
                }
            }
        });
        removeButton.setIcon(CoreConstants.ICON_DELETE);
        removeButton.setDisabled(true);

        grid = new ListGrid() {

            @Override
            protected Canvas getRollOverCanvas(Integer rowNum, Integer colNum) {

                rollOverRecord = this.getRecord(rowNum);

                if (rollOverCanvas == null) {
                    rollOverCanvas = new HLayout(3);
                    rollOverCanvas.setSnapTo("TR");
                    rollOverCanvas.setWidth(50);
                    rollOverCanvas.setHeight(22);

                    rollOverCanvas.addMember(FieldUtil.getImgButton(
                            CoreConstants.ICON_DELETE, "Remove user from this group",
                            new ClickHandler() {

                                @Override
                                public void onClick(ClickEvent event) {
                                    final String email = rollOverRecord.getAttribute("email");
                                    SC.ask("Do you really want to remove the user \""
                                            + email + "\" from this group?", new BooleanCallback() {

                                        @Override
                                        public void execute(Boolean value) {
                                            if (value) {
                                                removeUserFromGroup(email);
                                            }
                                        }
                                    });
                                }
                            }));
                }
                return rollOverCanvas;
            }
        };
        grid.setWidth(350);
        grid.setHeight100();
        grid.setShowRollOverCanvas(true);
        grid.setShowAllRecords(false);
        grid.setShowEmptyMessage(true);
        grid.setShowRowNumbers(true);
        grid.setCanHover(true);
        grid.setShowHover(true);
        grid.setShowHoverComponents(true);
        grid.setEmptyMessage("<br>No data available.");
        grid.setFields(FieldUtil.getIconGridField("countryCodeIcon"),
                new ListGridField("username", "Name"));
        grid.setSortField("username");
        grid.setSortDirection(SortDirection.ASCENDING);

        gridModal = new ModalWindow(grid);

        addField("Name", nameItem);
        this.addMember(FieldUtil.getForm(isPublicField));
        this.addMember(WidgetUtil.getLabel("<b>Users</b>", 15));
        this.addMember(grid);
        addButtons(saveButton, removeButton);
    }

    /**
     * Sets a group to edit or creates a blank form.
     *
     * @param name Group name
     */
    public void setGroup(String name, boolean isPublic) {

        if (name != null) {
            this.oldName = name;
            this.nameItem.setValue(name);
            this.isPublicField.setValue(isPublic);
            this.newGroup = false;
            this.removeButton.setDisabled(false);
            loadUsers();

        } else {
            this.oldName = null;
            this.nameItem.setValue("");
            this.isPublicField.setValue(true);
            this.newGroup = true;
            this.removeButton.setDisabled(true);
            this.grid.setData(new ListGridRecord[]{});
        }
    }

    private void save(String name, boolean isPublic) {

        ConfigurationServiceAsync service = ConfigurationService.Util.getInstance();

        if (newGroup) {
            modal.show("Adding " + name + "...", true);
            service.addGroup(new Group(name, isPublic), getCallback("add"));

        } else {
            modal.show("Updating " + name + "...", true);
            service.updateGroup(oldName, new Group(name, isPublic), getCallback("update"));
        }
    }

    private void remove(final String name) {

        if (name.equals(CoreConstants.GROUP_SUPPORT)) {
            SC.warn("You can not remove the " + name + " group.");
            return;
        }
        SC.ask("Do you really want to remove \"" + name + "\" group?", new BooleanCallback() {

            @Override
            public void execute(Boolean value) {
                if (value) {
                    ConfigurationServiceAsync service = ConfigurationService.Util.getInstance();
                    modal.show("Removing " + name + "...", true);
                    service.removeGroup(name, getCallback("remove"));
                }
            }
        });
    }

    private AsyncCallback<Void> getCallback(final String text) {

        return new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                modal.hide();
                SC.warn("Unable to " + text + " group:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                modal.hide();
                setGroup(null, false);
                ((ManageGroupsTab) Layout.getInstance().getTab(
                        CoreConstants.TAB_MANAGE_GROUPS)).loadGroups();
            }
        };
    }

    private void loadUsers() {

        ConfigurationServiceAsync service = ConfigurationService.Util.getInstance();
        final AsyncCallback<List<User>> callback = new AsyncCallback<List<User>>() {

            @Override
            public void onFailure(Throwable caught) {
                gridModal.hide();
                SC.warn("Unable to load users:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(List<User> result) {
                gridModal.hide();
                List<UserRecord> dataList = new ArrayList<UserRecord>();

                for (User user : result) {
                    dataList.add(new UserRecord(user));
                }
                grid.setData(dataList.toArray(new UserRecord[]{}));
            }
        };
        gridModal.show("Loading users from \"" + oldName + "\" group...", true);
        service.getUsersFromGroup(oldName, callback);
    }

    private void removeUserFromGroup(String email) {

        ConfigurationServiceAsync service = ConfigurationService.Util.getInstance();
        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                gridModal.hide();
                SC.warn("Unable to remove user:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                gridModal.hide();
                loadUsers();
            }
        };
        gridModal.show("Removing \"" + email + "\"<br />from \"" + oldName + "\"...", true);
        service.removeUserFromGroup(email, oldName, callback);
    }
}