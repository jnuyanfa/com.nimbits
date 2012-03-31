/*
 * Copyright (c) 2010 Tonic Solutions LLC.
 *
 * http://www.nimbits.com
 *
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, eitherexpress or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nimbits.client.ui.panels;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.nimbits.client.constants.UserMessages;
import com.nimbits.client.enums.EntityType;
import com.nimbits.client.enums.Parameters;
import com.nimbits.client.enums.SummaryType;
import com.nimbits.client.exception.NimbitsException;
import com.nimbits.client.model.common.CommonFactoryLocator;
import com.nimbits.client.model.entity.Entity;
import com.nimbits.client.model.entity.EntityName;
import com.nimbits.client.model.summary.Summary;
import com.nimbits.client.model.summary.SummaryModelFactory;
import com.nimbits.client.service.summary.SummaryService;
import com.nimbits.client.service.summary.SummaryServiceAsync;
import com.nimbits.client.ui.controls.EntityCombo;
import com.nimbits.client.ui.helper.FeedbackHelper;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Benjamin Sautner
 * User: BSautner
 * Date: 1/17/12
 * Time: 3:29 PM
 */
public class SummaryPanel extends NavigationEventProvider {

    FormData formdata;
    VerticalPanel vp;

    private Entity entity;
    private Summary summary;
    public SummaryPanel(Entity entity) {
        this.entity = entity;
    }
    @Override
    protected void onRender(final Element parent, final int index) {
        super.onRender(parent, index);
        // setLayout(new FillLayout());
        formdata = new FormData("-20");
        vp = new VerticalPanel();
        vp.setSpacing(10);

        if (entity != null) {
            if (entity.getEntityType().equals(EntityType.summary)) {
                getExistingSummary();
            }
            else {
                try {
                    createForm();
                    add(vp);
                    doLayout();
                } catch (NimbitsException e) {
                    FeedbackHelper.showError(e);
                }

            }

        }
        else {
            createNotFoundForm();
            doLayout();
        }


    }
    private void getExistingSummary() {
        SummaryServiceAsync service = GWT.create(SummaryService.class);
        service.readSummary(entity, new AsyncCallback<Summary>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(Summary result) {
                summary = result;
                try {
                    createForm();
                    add(vp);
                    doLayout();
                } catch (NimbitsException e) {
                    FeedbackHelper.showError(e);
                }

            }
        });
    }


    private ComboBox<SummaryTypeOption> summaryTypeOptionComboBox(final String title,final SummaryType selectedValue) {
        ComboBox<SummaryTypeOption> combo = new ComboBox<SummaryTypeOption>();

        ArrayList<SummaryTypeOption> ops = new ArrayList<SummaryTypeOption>();


        ops.add(new SummaryTypeOption(SummaryType.average));
        ops.add(new SummaryTypeOption(SummaryType.standardDeviation));
        ops.add(new SummaryTypeOption(SummaryType.min));
        ops.add(new SummaryTypeOption(SummaryType.max));
        ops.add(new SummaryTypeOption(SummaryType.skewness));
        ops.add(new SummaryTypeOption(SummaryType.sum));
        ops.add(new SummaryTypeOption(SummaryType.variance));




        ListStore<SummaryTypeOption> store = new ListStore<SummaryTypeOption>();

        store.add(ops);

        combo.setFieldLabel(title);
        combo.setDisplayField(Parameters.name.getText());
        combo.setValueField(Parameters.value.getText());
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setStore(store);

        SummaryTypeOption selected = combo.getStore().findModel(Parameters.value.getText(), selectedValue.getCode());
        combo.setValue(selected);

        return combo;

    }



    private void createForm() throws NimbitsException {

        FormPanel simple = new FormPanel();
        simple.setWidth(350);
        simple.setFrame(true);
        simple.setHeaderVisible(false);
        simple.setBodyBorder(false);
        simple.setFrame(false);

        final EntityName name;
        final TextField<String> summaryName = new TextField<String>();
        summaryName.setFieldLabel("Summary Name");
        try {
        if (summary != null && entity.getEntityType().equals(EntityType.summary)) {
            summaryName.setValue(entity.getName().getValue());
        }
        else {
            summaryName.setValue(entity.getName().getValue() + " Average");
        }

            name = CommonFactoryLocator.getInstance().createName(summaryName.getValue(), EntityType.summary);
        } catch (NimbitsException caught) {
            FeedbackHelper.showError(caught);
            return;
        }

       // int alertSelected = (subscription == null) ? SubscriptionNotifyMethod.none.getCode() : subscription.getAlertNotifyMethod().getCode();

        SummaryType type =  (summary == null) ? SummaryType.average : summary.getSummaryType() ;
        final ComboBox<SummaryTypeOption> typeCombo = summaryTypeOptionComboBox("Summary Type", type);







        final SpinnerField spinnerField = new SpinnerField();
        spinnerField.setIncrement(1d);
        spinnerField.getPropertyEditor().setType(Double.class);
        spinnerField.getPropertyEditor().setFormat(NumberFormat.getFormat("00"));
        spinnerField.setFieldLabel("Timespan (hours)");
        spinnerField.setMinValue(1d);
        spinnerField.setMaxValue(24d);
        spinnerField.setValue(summary == null ? 8 : summary.getSummaryIntervalHours());

        String target = summary == null ? null : summary.getTargetPointUUID();
        final EntityCombo targetCombo = new EntityCombo(EntityType.point, target, UserMessages.MESSAGE_SELECT_POINT );
        targetCombo.setFieldLabel("Target");

        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {


            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                try {
                    notifyEntityAddedListener(null);
                } catch (NimbitsException e) {
                    FeedbackHelper.showError(e);
                }
            }
        });

        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                SummaryServiceAsync service = GWT.create(SummaryService.class);
                final MessageBox box = MessageBox.wait("Progress",
                        "Create Summary", "please wit...");
                box.show();

                SummaryType summaryType =   typeCombo.getValue().getMethod();

                final Summary update;

                if (entity.getEntityType().equals(EntityType.summary) && summary != null) {

                    update = SummaryModelFactory.createSummary(summary.getUuid(),
                            summary.getEntity(), summary.getTargetPointUUID(), summaryType,
                            spinnerField.getValue().intValue() * 60 * 60 * 1000, new Date());

                }
                else {
                    update = SummaryModelFactory.createSummary(null,
                            entity.getEntity(),targetCombo.getValue().getUUID() , summaryType,
                            spinnerField.getValue().intValue() * 60 * 60 * 1000, new Date());

                }


                service.addUpdateSummary(entity, update, name, new AsyncCallback<Entity>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        FeedbackHelper.showError(caught);
                        box.close();
                        try {
                            notifyEntityAddedListener(null);
                        } catch (NimbitsException e) {
                            FeedbackHelper.showError(e);
                        }
                    }

                    @Override
                    public void onSuccess(Entity result) {
                        box.close();
                        try {
                            notifyEntityAddedListener(result);
                        } catch (NimbitsException e) {
                            FeedbackHelper.showError(e);
                        }
                    }
                });

            }
        });


        Html h = new Html("<p>The summation process runs once an hour and can compute a summary value (such as an average) " +
                "based on the interval you set here (i.e a setting of 8 will computer an 8 hour average every 8 hours) using the " +
                "data recorded to the selected data point, storing the result in the select pre-existing target point.</p>");


        Html pn = new Html("<p><b>Name: </b>" + entity.getName().getValue() + "</p>");





        vp.add(h);
        vp.add(pn);
        simple.add(summaryName, formdata);
        simple.add(typeCombo, formdata);

        simple.add(spinnerField, formdata);
        simple.add(targetCombo, formdata);
        LayoutContainer c = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayout.BoxLayoutPack.END);
        c.setLayout(layout);
        cancel.setWidth(100);
        submit.setWidth(100);
        HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 5, 0, 0));
        c.add(cancel, layoutData);
        c.add(submit, layoutData);



        vp.add(simple);
        vp.add(c);
    }

    private void createNotFoundForm() {



        Html h = new Html("<p>Sorry, the UUID provided for the point you're trying to subscribe too " +
                " can not be located. It has either been deleted or marked as private by the point owner.");




        vp.add(h);

    }


    private class SummaryTypeOption extends BaseModelData {
        SummaryType type;


        public SummaryTypeOption(SummaryType value) {
            this.type = value;
            set(Parameters.value.getText(), value.getCode());
            set(Parameters.name.getText(), value.getText());
        }

        public SummaryType getMethod() {
            return type;
        }
    }

}
